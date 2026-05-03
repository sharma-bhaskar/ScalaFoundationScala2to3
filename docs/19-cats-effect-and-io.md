# Chapter 19 — Cats Effect and the IO Monad

> The Cats Effect library replaces `Future` with `IO[A]` — a *description* of an async computation that you run on demand. Referentially transparent, cancellable, resource-safe.

**In this chapter:**
1. [Why `IO` over `Future`](#1-why-io-over-future)
2. [Building and running an `IO`](#2-building-and-running-an-io)
3. [Sequencing with for-comprehensions](#3-sequencing-with-for-comprehensions)
4. [Concurrency: fibers, `parMapN`, `race`, `parTraverse`](#4-concurrency-fibers-parmapn-race-partraverse)
5. [Resource safety with `Resource`](#5-resource-safety-with-resource)
6. [Cancellation](#6-cancellation)
7. [`IOApp` for entry points](#7-ioapp-for-entry-points)
8. [A brief mention of ZIO](#8-a-brief-mention-of-zio)

---

## 1. Why `IO` over `Future`

`Future` is *eager*: the moment you write `Future { work() }`, work has started. This breaks referential transparency:

```scala
val f1 = Future { println("running"); 42 }
val f2 = Future { println("running"); 42 }
// "running" prints twice — they're different futures

val f3 = { println("running"); Future.successful(42) }
val combined = (f3, f3).mapN(_ + _)
// "running" prints ONCE — f3 is a value
```

Two textually identical expressions don't behave the same. That's a non-trivial cognitive cost.

`IO[A]` is *lazy*: it's a *description* of work. Equality of descriptions is straightforward. Building an `IO` doesn't run anything; you must call an explicit "run" at the edge of your program.

```scala
import cats.effect.IO

val io: IO[Int] = IO {
  println("running")
  42
}
// nothing prints — io is just a value

io.unsafeRunSync()    // NOW it runs
io.unsafeRunSync()    // runs again
```

That referential transparency makes refactoring safe and reasoning local — you can replace any `IO` value with another value of the same description without changing meaning.

Setup:

```scala
// build.sbt
libraryDependencies += "org.typelevel" %% "cats-effect" % "3.5.4"
```

---

## 2. Building and running an `IO`

```scala
import cats.effect.IO

val pure: IO[Int]              = IO.pure(42)              // already-known value, no work
val delayed: IO[Int]           = IO.delay { 1 + 1 }        // suspended sync work
val sleeping: IO[Unit]         = IO.sleep(1.second)        // sleep without blocking a thread
val async: IO[String]          = IO.async_[String] { cb => 
  // callback-based bridge: call cb(Right(...)) or cb(Left(...))
}
```

Run at the edge:

```scala
io.unsafeRunSync()       // blocks until done, returns the result
io.unsafeToFuture()      // returns a Scala Future
io.unsafeRunAsync(callback)
```

The `unsafe*` prefix is intentional — these break referential transparency and should appear *only* at your application's entry point.

---

## 3. Sequencing with for-comprehensions

`IO` is a Monad. For-comprehensions work as you'd expect:

```scala
val program: IO[Unit] = for {
  _ <- IO.println("hello")
  n <- IO(42)
  _ <- IO.println(s"got $n")
  _ <- IO.println("bye")
} yield ()

program.unsafeRunSync()
// hello
// got 42
// bye
```

Each step runs in sequence. `flatMap` and `map` work; the for-comp desugars normally.

You can compose IOs into bigger programs from small ones:

```scala
def fetch(url: String): IO[String] = ???
def parse(s: String): IO[Json] = ???
def save(j: Json): IO[Unit] = ???

val workflow: IO[Unit] = for {
  body <- fetch("/api/data")
  json <- parse(body)
  _    <- save(json)
} yield ()
```

---

## 4. Concurrency: fibers, `parMapN`, `race`, `parTraverse`

A **fiber** is a lightweight thread — Cats Effect runs millions on a small JVM thread pool. Forking is cheap.

### `parMapN` — run independent IOs in parallel

```scala
import cats.implicits._

val fa: IO[Int] = IO { Thread.sleep(100); 1 }
val fb: IO[Int] = IO { Thread.sleep(100); 2 }
val fc: IO[Int] = IO { Thread.sleep(100); 3 }

val parallel: IO[Int] = (fa, fb, fc).parMapN(_ + _ + _)
// total time ≈ 100ms, not 300ms
```

Compared to `(fa, fb, fc).mapN(_ + _ + _)` which would be sequential, `parMapN` runs them on separate fibers.

### `race` — first one wins

```scala
val first: IO[Either[Int, String]] =
  IO.race(IO.sleep(1.second).as(42), IO.sleep(2.seconds).as("late"))
```

`Either[A, B]` — `Left` if the first IO won, `Right` if the second. The losing IO is **automatically cancelled**, freeing resources. (Compare: `Future.firstCompletedOf` keeps losers running.)

### `parTraverse` — run a function over a collection in parallel

```scala
val urls = List("/a", "/b", "/c")
def fetch(url: String): IO[String] = ???

val responses: IO[List[String]] = urls.parTraverse(fetch)
```

Or with bounded parallelism:

```scala
urls.parTraverseN(8)(fetch)    // at most 8 in flight
```

This is one of the most useful patterns in Cats Effect.

### Manual fibers

If you need fine control:

```scala
val fork: IO[Unit] = for {
  fiber <- IO.println("background...").start    // spawn a fiber
  _     <- IO.println("foreground")
  _     <- fiber.join                            // wait for it
  // or fiber.cancel
} yield ()
```

For most code, `parMapN` / `parTraverse` are enough. Drop to `start` only when you need to fire-and-forget or store fiber refs.

---

## 5. Resource safety with `Resource`

`Resource[F, A]` represents an acquire/release pair. Use it for anything you'd traditionally `try { ... } finally { close() }`:

```scala
import cats.effect.Resource

def fileResource(path: String): Resource[IO, java.io.BufferedReader] =
  Resource.make(
    IO(new java.io.BufferedReader(new java.io.FileReader(path)))
  )(reader => IO(reader.close()))

val readFirstLine: IO[String] =
  fileResource("/etc/hosts").use { reader =>
    IO(reader.readLine())
  }
// the file is automatically closed afterward, even on error
```

`Resource`s compose:

```scala
def db: Resource[IO, Connection] = ???
def kafka: Resource[IO, Producer] = ???

val both: Resource[IO, (Connection, Producer)] =
  for {
    c <- db
    p <- kafka
  } yield (c, p)

both.use { case (conn, prod) =>
  // both are open; both close when use exits
  ???
}
```

This is the correct way to manage thread pools, DB connections, HTTP clients, file handles. It's leak-proof.

---

## 6. Cancellation

Unlike `Future`, an `IO` can be cancelled. Built-in operations like `IO.sleep` are interruptible. Long-running computations should periodically check for cancellation:

```scala
val long: IO[Unit] = IO.tailRecM(0) { n =>
  if (n >= 1_000_000) IO.pure(Right(()))
  else IO.cede *> IO.pure(Left(n + 1))    // IO.cede is a cancellation point
}

val raced = IO.race(long, IO.sleep(100.millis))
```

`IO.cede` yields the fiber, giving the runtime a chance to deliver cancellation. Most libraries do this transparently.

Cancellation is *cooperative* — the running fiber must check. But all of Cats Effect's built-in async operations check, so for normal code you get cancellation for free.

---

## 7. `IOApp` for entry points

The clean way to launch an `IO` program:

```scala
import cats.effect.{IO, IOApp, ExitCode}

object Main extends IOApp.Simple {
  val run: IO[Unit] =
    for {
      _ <- IO.println("hello, IO world")
      _ <- IO.sleep(1.second)
      _ <- IO.println("bye")
    } yield ()
}
```

Or the version that takes args and returns an `ExitCode`:

```scala
object Main extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    if (args.isEmpty) IO.println("missing arg").as(ExitCode.Error)
    else IO.println(s"hi ${args.head}").as(ExitCode.Success)
}
```

`IOApp` handles graceful shutdown, fiber cleanup, and signal handling for you.

---

## 8. A brief mention of ZIO

[ZIO](https://zio.dev/) is the other major effect system in Scala. Same goals as Cats Effect; different API and ergonomics.

The headline difference: ZIO's effect type is `ZIO[R, E, A]` — three parameters:

- `R` — the *requirements* (dependencies / environment).
- `E` — the typed *error*.
- `A` — the result.

This typed environment is ZIO's signature feature; Cats Effect models the same thing differently (typically with `Reader` or `Kleisli` patterns).

If you start a new project, **pick one** based on the team's preference and stick with it. Mixing both is a recipe for fragmentation. Both are excellent.

---

## What you should now know

- Why `IO` is referentially transparent and `Future` isn't — and why that matters.
- How to build and run `IO`s, including the `unsafe*` boundary.
- For-comprehensions over `IO` for sequential composition.
- `parMapN`, `race`, `parTraverse` for parallelism with automatic cancellation.
- `Resource` for leak-proof acquire/release.
- Cooperative cancellation via `IO.cede` and built-in async ops.
- `IOApp` as the standard entry point.
- That ZIO exists as an alternative.

The next chapter ([Chapter 20 — Actors and Streams](20-actors-and-streams.md)) is a brief tour of the actor model and Akka Streams.

---

[← Previous: Chapter 18 — Futures](18-concurrency-futures.md) | [Back to README](README.md) | [Next: Chapter 20 — Actors and Streams →](20-actors-and-streams.md)
