# Chapter 18 — Futures and Promises

> Scala's stdlib `Future[A]` is the workhorse for async work. Easy to start with, easy to misuse. This chapter covers the API, the gotchas, and when to upgrade to a real effect system.

**In this chapter:**
1. [`Future[A]` basics](#1-futurea-basics)
2. [The `ExecutionContext`](#2-the-executioncontext)
3. [Combinators: `map`, `flatMap`, `recover`, `zip`](#3-combinators-map-flatmap-recover-zip)
4. [Sequential vs parallel composition](#4-sequential-vs-parallel-composition)
5. [`Promise[A]` for bridging callback APIs](#5-promisea-for-bridging-callback-apis)
6. [`Await` and why you shouldn't use it](#6-await-and-why-you-shouldnt-use-it)
7. [`blocking { ... }` for blocking calls](#7-blocking---for-blocking-calls)
8. [Common pitfalls](#8-common-pitfalls)

---

## 1. `Future[A]` basics

```scala
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global   // a default EC

val f: Future[Int] = Future {
  Thread.sleep(1000)    // pretend work
  42
}

f.foreach(println)   // prints 42 when ready
```

A `Future[A]` is a placeholder for a value that will be available later. It's *eager* — `Future { ... }` starts computing immediately on the EC.

Common factory:

```scala
Future.successful(42)         // already-completed success
Future.failed(new Exception)  // already-completed failure
Future.unit                   // Future[Unit] — already done
Future(work())                // start an async computation
```

---

## 2. The `ExecutionContext`

`Future` doesn't have its own threads — it submits tasks to an `ExecutionContext`. You need one in scope (as an implicit) for nearly every Future operation.

```scala
import scala.concurrent.ExecutionContext.Implicits.global

Future { 1 + 1 }    // uses global EC
```

The `global` EC is a `ForkJoinPool`. Fine for development and CPU-bound tasks; **bad for blocking I/O** (a few blocked threads can starve the whole pool — see section 7 below).

For real apps, define your own:

```scala
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

val ec: ExecutionContext = ExecutionContext.fromExecutor(
  Executors.newFixedThreadPool(16)
)
```

**Pass the EC explicitly when you can** rather than relying on global. It makes thread allocation auditable.

---

## 3. Combinators: `map`, `flatMap`, `recover`, `zip`

```scala
val a: Future[Int] = Future(2)

a.map(_ * 10)                        // Future(20)
a.flatMap(n => Future(n + 1))        // Future(3)
a.filter(_ > 0)                      // Future(2); fails if predicate false

a.recover { case _: ArithmeticException => 0 }
a.recoverWith { case _: ArithmeticException => Future(0) }

a.fallbackTo(Future(0))              // tries a, falls back to Future(0)
a.transform(Success(_), identity)    // map both sides

val b: Future[String] = Future("hi")
a.zip(b)                             // Future((2, "hi"))
```

For-comprehension as you'd expect:

```scala
for {
  x <- Future(2)
  y <- Future(3)
} yield x + y    // Future(5)
```

**Failure handling** is via `recover`/`recoverWith`/`transform`. A failed Future stays failed unless you explicitly recover:

```scala
val bad = Future { throw new Exception("boom") }
bad.foreach(println)         // never runs
bad.failed.foreach(println)  // prints the exception
```

---

## 4. Sequential vs parallel composition

This is the gotcha in [Chapter 15](15-for-comprehensions.md), worth re-emphasizing:

```scala
// SEQUENTIAL — each future waits for the previous
val sequential = for {
  a <- fetch("/a")
  b <- fetch("/b")
  c <- fetch("/c")
} yield (a, b, c)
```

If you don't depend on `a` to make `b`, this is wasteful. Run them in parallel:

```scala
// PARALLEL — all three start before any awaits
val fa = fetch("/a")
val fb = fetch("/b")
val fc = fetch("/c")

val parallel = for { a <- fa; b <- fb; c <- fc } yield (a, b, c)
```

Or use `zip` / `Future.sequence`:

```scala
Future.sequence(List(fa, fb, fc))    // Future[List[Response]]
fa.zip(fb).map { case (a, b) => ... }
```

If you have Cats, `parTraverse` does the same with arbitrary parallelism control:

```scala
import cats.implicits._
List("/a", "/b", "/c").parTraverse(fetch)
```

---

## 5. `Promise[A]` for bridging callback APIs

A `Promise[A]` is the *write* side of a Future. Useful for adapting callback-based APIs:

```scala
import scala.concurrent.Promise

def fromCallback(api: (String => Unit, Throwable => Unit) => Unit): Future[String] = {
  val p = Promise[String]()
  api(
    onSuccess = result => p.success(result),
    onFailure = err    => p.failure(err)
  )
  p.future
}
```

You complete the promise once, succeeding or failing it; whoever has its `.future` sees the result.

A common use: wrapping a JDBC `Future`-style callback into a Scala Future, or wrapping a Java `CompletableFuture`:

```scala
import java.util.concurrent.CompletableFuture
import scala.concurrent.Future

def toScala[A](cf: CompletableFuture[A]): Future[A] = {
  val p = Promise[A]()
  cf.whenComplete { (value, err) =>
    if (err == null) p.success(value)
    else p.failure(err)
  }
  p.future
}
```

In Scala 2.13+, `scala.jdk.FutureConverters` does this for you:

```scala
import scala.jdk.FutureConverters._
val sf: Future[A] = cf.asScala
```

---

## 6. `Await` and why you shouldn't use it

`Await.result` blocks the calling thread until the Future completes:

```scala
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

val v = Await.result(Future(42), 5.seconds)
println(v)
```

**Don't use Await in production.** It defeats the entire point of async. The acceptable cases:

- The very top of `main` in a CLI app.
- Test code that needs to assert on a Future's result.
- Bridging an async API to a synchronous Java caller.

Anywhere else, threading the Future through the call chain is correct.

---

## 7. `blocking { ... }` for blocking calls

If you must do a blocking operation inside a Future, wrap it:

```scala
import scala.concurrent.blocking

Future {
  blocking {
    Thread.sleep(1000)        // or JDBC, or a slow Java API
  }
  42
}
```

The `blocking` block is a *hint* to the EC to spin up an extra thread if it can — preventing total starvation. It's not a guarantee. The default `ForkJoinPool` honors it; a `FixedThreadPool` won't.

The right answer for serious blocking: a separate, dedicated EC with a generous thread count, used only for blocking work:

```scala
val blockingEC = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(64))

def queryDb(): Future[Result] = Future(jdbcCall())(blockingEC)   // explicit EC
```

This is the same pattern as Akka's "use a blocking dispatcher" idea ([Akka post](#)).

---

## 8. Common pitfalls

### Eager evaluation

`Future { work() }` runs `work()` immediately. There's no "start" — the future is already going. If you wanted to defer, use a function:

```scala
val later: () => Future[Int] = () => Future(expensive())
// use later() to start
```

This is one of the reasons FP people prefer `IO` (next chapter) — `IO` is *referentially transparent*; `Future` isn't.

### Swallowed exceptions

A failed Future without a `recover`/`onComplete`/`failed.foreach` silently goes nowhere. Always handle failure:

```scala
val f = Future { throw new Exception("boom") }
f                            // exception silently held
f.failed.foreach(_.printStackTrace)
```

Or better, log via `transform` / your logging framework.

### Capturing a `var` in a Future

```scala
var counter = 0
val f1 = Future { Thread.sleep(100); counter += 1; counter }
val f2 = Future { Thread.sleep(100); counter += 1; counter }
```

Two threads racing on `counter`. Use `AtomicInteger` or — better — restructure to avoid shared state.

### The "Future cancellation" myth

You can't cancel a `Future`. Once started, it runs to completion (unless it throws). Any "cancel" you see in libraries is a workaround. If you need cancellation, use Cats Effect `IO` or ZIO — both have first-class cancellation.

### `Future.firstCompletedOf` doesn't cancel losers

```scala
Future.firstCompletedOf(List(slow, fast))    // resolves with `fast`'s value
// but `slow` keeps running, wasting resources
```

Same root cause as above.

---

## What you should now know

- `Future[A]` is an eagerly-started async computation, with `flatMap`/`map` for composition.
- The `ExecutionContext` is the thread pool; pass it explicitly in production.
- For-comprehension on `Future`s is sequential; for parallel, kick off futures *before* the `for`.
- `Promise[A]` for bridging callback-based APIs.
- `Await.result` is for boundaries only — never in business logic.
- `blocking { ... }` is a hint; a dedicated EC is the real fix.
- The big pitfalls: eager evaluation, swallowed exceptions, no cancellation.

The next chapter ([Chapter 19 — Cats Effect](19-cats-effect-and-io.md)) is the FP-style fix to most of these problems: `IO` is referentially transparent, cancellable, and resource-safe.

---

[← Previous: Chapter 17 — Monads](17-monads-and-fp-abstractions.md) | [Back to README](README.md) | [Next: Chapter 19 — Cats Effect →](19-cats-effect-and-io.md)
