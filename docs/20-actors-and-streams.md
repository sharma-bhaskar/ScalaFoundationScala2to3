# Chapter 20 — Actors and Streams (intro)

> A short tour of Akka actors and Akka Streams. For depth, see the dedicated Akka post.

**In this chapter:**
1. [The actor model in 60 seconds](#1-the-actor-model-in-60-seconds)
2. [A minimal Akka Typed actor](#2-a-minimal-akka-typed-actor)
3. [Akka Streams basics](#3-akka-streams-basics)
4. [Source / Flow / Sink](#4-source--flow--sink)
5. [When to reach for actors vs effects vs streams](#5-when-to-reach-for-actors-vs-effects-vs-streams)
6. [Pekko: the OSS fork](#6-pekko-the-oss-fork)

---

## 1. The actor model in 60 seconds

An **actor** is a unit of work + state + behavior. It:

- Has a private mailbox.
- Reads one message at a time.
- Holds private state nobody else can touch.
- Reacts by changing its behavior, sending messages, or spawning children.

Many actors share a small thread pool. Communication is exclusively by message. No shared state, no locks.

This is the model behind Akka, Pekko, Erlang, and the actor systems baked into many platforms. Scala's flagship actor library is **Akka** (now under the BSL license) and its Apache fork **Pekko**.

For the full deep-dive, see the dedicated Akka post — internals, supervision, mailbox algorithms, clustering, sharding, persistence.

---

## 2. A minimal Akka Typed actor

```scala
// build.sbt
// libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5"

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object Greeter {
  sealed trait Command
  final case class Greet(name: String) extends Command

  def apply(): Behavior[Command] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case Greet(name) =>
        ctx.log.info(s"hello, $name")
        Behaviors.same
    }
  }
}

object Main extends App {
  val system: ActorSystem[Greeter.Command] =
    ActorSystem(Greeter(), "demo")

  system ! Greeter.Greet("Bhaskar")
  system ! Greeter.Greet("world")
}
```

The actor's protocol is a sealed type — the compiler refuses anything else. State changes come from returning a different `Behavior`. Tell (`!`) is fire-and-forget; ask (`?`) returns a `Future[Reply]`.

---

## 3. Akka Streams basics

A **stream** is a typed pipeline with end-to-end **back-pressure**. Slow consumers slow down fast producers automatically — no OOM from a producer outpacing a sink.

```scala
import akka.stream.scaladsl._
import akka.{NotUsed, Done}

val pipeline: Source[Int, NotUsed] = Source(1 to 1_000_000)

val result: Future[Done] =
  pipeline
    .filter(_ % 2 == 0)
    .map(_ * 10)
    .runWith(Sink.foreach(println))
```

Build a graph from `Source`, `Flow`, `Sink`. Run it on a `Materializer` (one is provided by the `ActorSystem`).

---

## 4. Source / Flow / Sink

Three building blocks:

```scala
val source: Source[Int, NotUsed] = Source(1 to 100)        // produces values
val flow:   Flow[Int, String, NotUsed] = Flow[Int].map(_.toString)  // transforms
val sink:   Sink[String, Future[Done]] = Sink.foreach(println)      // consumes

val runnable: RunnableGraph[Future[Done]] = source.via(flow).to(sink)
```

A `RunnableGraph` is "fully wired but not running yet." Materialize it:

```scala
val materializedValue: Future[Done] = runnable.run()
```

### Common operators

```scala
Source(1 to 100)
  .map(_ * 2)
  .filter(_ > 10)
  .grouped(10)                     // batch into chunks of 10
  .mapAsync(parallelism = 4)(batch => Future(saveBatch(batch)))
  .recover { case e => /* fallback element */ ??? }
  .runWith(Sink.ignore)
```

Operators of note:

- `mapAsync(p)` — apply an async function with at most `p` concurrent in-flight.
- `grouped(n)` — chunk into batches of n.
- `throttle(n, per)` — rate limit.
- `runWith` / `runForeach` / `runFold` — terminal operations.

For real I/O, **Alpakka** (Akka Streams' connector library) provides Sources/Sinks for Kafka, S3, JDBC, HTTP, file, FTP, gRPC, etc.

---

## 5. When to reach for actors vs effects vs streams

A pragmatic decision guide:

| Need | Tool |
|---|---|
| Stateful, message-driven, distributed processing | **actors** |
| Pipelines with back-pressure, transformations | **streams** |
| Pure async value transformations, one-shot | **`IO` / Cats Effect** or `Future` |
| Long-running stateful entity sharded across cluster | **actors + sharding + persistence** |
| Reactive, "infinite" streams of data | **streams** |
| Concurrent function combinators (race, par, retry) | **`IO`** |
| Workflows with timeouts/retries on individual steps | **`IO`** with combinators |

Actors and streams aren't competitors — Akka Streams is built on actors, and they share an ActorSystem. They're different shapes for different problems.

---

## 6. Pekko: the OSS fork

Akka 2.7+ ships under the **Business Source License** (BSL 1.1), not Apache 2.0. Many companies migrated to **Apache Pekko**, the community fork picked up from Akka 2.6.

The migration is mostly a search/replace: `akka.*` → `org.apache.pekko.*`. The APIs are identical.

```scala
// Akka:
import akka.actor.typed.ActorSystem

// Pekko (same code):
import org.apache.pekko.actor.typed.ActorSystem
```

If you're starting a new project that needs to stay open-source, choose Pekko. If you're already invested in Akka with a license, stay there.

---

## What you should now know

- The actor model: mailbox, behavior, no shared state.
- A minimal Akka Typed actor (Behavior + protocol).
- Akka Streams as a back-pressured pipeline of `Source` / `Flow` / `Sink`.
- The decision tree for actors vs streams vs IO.
- The Akka/Pekko license situation.

The next chapter ([Chapter 21 — Macros and Metaprogramming](21-macros-and-metaprogramming.md)) covers compile-time programming.

---

[← Previous: Chapter 19 — Cats Effect](19-cats-effect-and-io.md) | [Back to README](README.md) | [Next: Chapter 21 — Macros →](21-macros-and-metaprogramming.md)
