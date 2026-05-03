# Chapter 26 — Cheatsheet

> One-page quick reference for everything in this guide. Skim it before an interview; ctrl-F it during a code review.

**In this chapter:**
1. [Bindings & basics](#1-bindings--basics)
2. [Functions](#2-functions)
3. [Collections](#3-collections)
4. [Pattern matching](#4-pattern-matching)
5. [OOP](#5-oop)
6. [Type system](#6-type-system)
7. [Implicits / Givens](#7-implicits--givens)
8. [FP & error handling](#8-fp--error-handling)
9. [Concurrency](#9-concurrency)
10. [Scala 2 → Scala 3 migration table](#10-scala-2--scala-3-migration-table)
11. [Top 30 stdlib methods](#11-top-30-stdlib-methods)

---

## 1. Bindings & basics

```scala
val x = 1                // immutable
var y = 2                // mutable
lazy val z = compute()   // initialized on first use, exactly once
def f = ???              // method (re-evaluated)

// types: Any > AnyVal | AnyRef > Nothing/Null
// Unit ≡ ()    Nothing ≡ no value (throws/loops)

// literals
val n: Long      = 42L
val d: Double    = 3.14
val readable     = 1_000_000
val s            = s"hi $name, math: ${1+1}"
val multi        = """line 1
                     |line 2""".stripMargin
val raw          = raw"\d+"
val fstring      = f"$pi%.2f"
```

---

## 2. Functions

```scala
def add(a: Int, b: Int): Int = a + b
val addV: (Int, Int) => Int = (a, b) => a + b

// HOF + currying + partial app
def adder(n: Int): Int => Int = x => x + n
def addCurried(a: Int)(b: Int): Int = a + b
val plus5 = addCurried(5) _

// by-name
def myAssert(c: Boolean, msg: => String): Unit = if (!c) throw new Error(msg)

// eta-expansion
List(1, 2).map(double)            // method auto-converts to function

// _ placeholder
List(1,2,3).map(_ * 2)
List(1,2,3).foldLeft(0)(_ + _)

// function composition
val incr: Int => Int = _ + 1
val dbl:  Int => Int = _ * 2
incr andThen dbl    // dbl(incr(x))
incr compose dbl    // incr(dbl(x))
```

---

## 3. Collections

```scala
// hierarchy: Iterable > Seq > IndexedSeq | LinearSeq
//            Iterable > Set, Map

List(1, 2, 3)        Vector(1, 2, 3)        Array(1, 2, 3)
Set(1, 2, 3)         Map("a" -> 1, "b" -> 2)
1 to 5               1 until 5              1 to 10 by 2
LazyList.from(1)     // infinite, memoizing
Iterator(1, 2, 3)    // one-shot

// transform
xs.map(f); xs.flatMap(f); xs.filter(p)
xs.collect { case n if n > 0 => n * 10 }
xs.zip(ys); xs.zipWithIndex
xs.foldLeft(0)(_ + _); xs.reduce(_ + _)
xs.groupBy(_.head)
xs.groupMapReduce(key)(value)(combine)

// other
xs.distinct; xs.sorted; xs.sortBy(_.field); xs.sortWith(_<_)
xs.take(n); xs.drop(n); xs.takeWhile(p); xs.dropWhile(p); xs.span(p)
xs.exists(p); xs.forall(p); xs.find(p); xs.count(p)
xs.head; xs.headOption; xs.last; xs.tail; xs.init

// view (lazy ops)
xs.view.map(f).filter(p).take(5).toList
```

---

## 4. Pattern matching

```scala
x match {
  case 0                          => "zero"        // literal
  case n: Int                     => s"int $n"     // type
  case Some(v)                    => v             // constructor
  case (a, b)                     => a + b         // tuple
  case head :: rest               => ...           // sequence
  case List(_, _, _*)             => "3+ items"
  case n if n > 0                 => "positive"    // guard
  case adult @ User(_, age) if age > 18 => adult.name  // @ binding
  case `pi`                       => "pi const"    // backticks
  case _                          => "other"
}

// custom extractor
object Email {
  def unapply(s: String): Option[(String, String)] = ???
}

// partial function
val pf: PartialFunction[Int, String] = { case n if n > 0 => s"+$n" }
xs.collect(pf)
```

---

## 5. OOP

```scala
class Person(val name: String, var age: Int)        // primary ctor
case class Order(id: String, items: List[String])   // synthesized eq/hash/toString/copy/apply/unapply

object Logger {                                      // singleton
  def log(msg: String): Unit = println(msg)
}

class Account private (val id: String) { ... }       // companion-only construction
object Account {
  def open(id: String) = new Account(id)
}

trait Greeter { def greet(): String }                // interface with impls
class English extends Greeter with Logger { ... }    // mix in

sealed trait Result                                  // ADT
case class Success(v: Int) extends Result
case object Failure          extends Result

// scala 3:
enum Day:
  case Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday

// visibility
private val x         // class only
private[this] val x   // this-instance only
protected val x       // subclass too
private[pkg] val x    // package-private
```

---

## 6. Type system

```scala
// hierarchy: Any > AnyVal | AnyRef > Nothing/Null

type UserId = String                  // alias

class Box[+A]                         // covariant: Box[Dog] <: Box[Animal]
class Sink[-A]                        // contravariant: Sink[Animal] <: Sink[Dog]
class Cell[A]                         // invariant

def f[A <: Comparable[A]](x: A): A    // upper bound (F-bounded)
def g[A >: Null](x: A): A             // lower bound
def h[A : Ordering](x: A): A          // context bound (implicit Ordering[A])

// scala 3 only:
opaque type UserId = String           // zero-cost wrapper
type Result = Int | String            // union
type With   = Resettable & Growable   // intersection
```

---

## 7. Implicits / Givens

```scala
// scala 2
implicit val ec: ExecutionContext = ...
implicit def listShow[A](implicit s: Show[A]): Show[List[A]] = ...
implicit class IntOps(val n: Int) extends AnyVal {
  def squared: Int = n * n
}
def render[A](a: A)(implicit s: Show[A]): String = s.show(a)
def render2[A: Show](a: A): String = implicitly[Show[A]].show(a)

// scala 3
given ec: ExecutionContext = ...
given listShow[A](using s: Show[A]): Show[List[A]] with
  def show(xs: List[A]): String = ...
extension (n: Int) def squared: Int = n * n
def render[A](a: A)(using s: Show[A]): String = s.show(a)
def render2[A: Show](a: A): String = summon[Show[A]].show(a)
```

---

## 8. FP & error handling

```scala
// Option
val o: Option[Int] = Some(1)
o.getOrElse(0); o.map(_+1); o.flatMap(...)
o.fold(default)(transform)
o match { case Some(n) => ?; case None => ? }
parseInt(s).toRight("bad")              // Option -> Either

// Either (right-biased)
val e: Either[String, Int] = Right(1)
e.map(_+1); e.flatMap(...); e.getOrElse(0)
e.fold(handleLeft, handleRight)

// Try
import scala.util.{Try, Success, Failure}
Try(parse(s)).toEither.left.map(_.getMessage)

// for-comp short-circuits on first None/Left/Failure
for { a <- maybeA; b <- maybeB } yield a + b

// recursion
@tailrec def loop(rem: List[Int], acc: Int): Int = rem match {
  case Nil      => acc
  case h :: t   => loop(t, acc + h)
}

// ADT
sealed trait Shape
case class Circle(r: Double)         extends Shape
case class Rectangle(w: Double, h: Double) extends Shape
```

---

## 9. Concurrency

```scala
// Future (eager, not cancellable, requires EC)
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global

val f = Future { work() }
f.map(_+1).recover { case e => 0 }
val combined = (fa, fb).mapN(_ + _)        // parallel via cats

// IO (lazy, referentially transparent, cancellable)
import cats.effect.IO
val io: IO[Int] = IO { 42 }
val seq    = for { a <- ioA; b <- ioB } yield a + b   // sequential
val par    = (ioA, ioB).parMapN(_ + _)                 // parallel
val race   = IO.race(ioA, ioB)                          // first wins, loser cancelled
val resAll = listOfThings.parTraverse(processOne)       // parallel collection

// Resource
import cats.effect.Resource
def file: Resource[IO, BufferedReader] =
  Resource.make(IO(open()))(r => IO(r.close()))

file.use { r => IO(r.readLine()) }                     // automatic cleanup

// Akka Typed actor (sketch)
def behavior: Behavior[Cmd] = Behaviors.receive { (ctx, msg) =>
  msg match { case Greet(name) => ctx.log.info(s"hi $name"); Behaviors.same }
}
```

---

## 10. Scala 2 → Scala 3 migration table

| Scala 2 | Scala 3 |
|---|---|
| `implicit val x: T = ...` | `given x: T = ...` |
| `implicit def f(implicit x: T): U` | `given f(using x: T): U` |
| `def m(implicit x: T)` | `def m(using x: T)` |
| `implicitly[T]` | `summon[T]` |
| `implicit class FooOps(val n: Int)` | `extension (n: Int) def ...` |
| `implicit def conv(s: String): Int = ...` | `given Conversion[String, Int] = _.toInt` |
| `sealed trait Day; case object Mon extends Day; ...` | `enum Day { case Mon, Tue, ... }` |
| `if (cond) a else b` | `if cond then a else b` |
| `try { ... } catch { case e => ... }` | `try ... catch case e => ...` |
| `for { x <- xs } yield x` | (same) |
| `class Foo { ... }` | `class Foo: ...` (indented) |
| `Type[X] forSome { ... }` | (removed; use wildcards or type params) |
| `({type L[A] = F[String, A]})#L` | `[A] =>> F[String, A]` |
| `import a._` (for givens) | `import a.given` |

For automatic migration, install **scalafix** with `Scala3Lint` and run `sbt scalafixAll` plus `scalafmt`.

---

## 11. Top 30 stdlib methods

The methods you'll actually use every day, by container.

### `Option[A]`
```scala
.map .flatMap .getOrElse .orElse .fold .filter .foreach .toRight
.exists .forall .isDefined .isEmpty .nonEmpty .toList
```

### `Either[L, R]` (right-biased)
```scala
.map .flatMap .fold .getOrElse .swap .toOption .left.map
```

### `Try[A]`
```scala
.map .flatMap .recover .recoverWith .getOrElse .toEither .toOption
```

### `List[A]` / `Vector[A]` / `Iterable[A]`
```scala
.map .flatMap .filter .filterNot .collect
.foldLeft .foldRight .reduce .reduceOption
.head .headOption .tail .last .init
.take .drop .takeWhile .dropWhile .span
.find .exists .forall .count
.distinct .sorted .sortBy .sortWith .reverse
.zip .zipWithIndex .partition .groupBy .groupMapReduce
.mkString .toMap .toSet .toVector .toList
```

### `Map[K, V]`
```scala
.get .getOrElse .contains .keys .values
+ - ++ -- updated removed
.mapValues .filterKeys .view.mapValues(...).toMap
```

### `Future[A]`
```scala
.map .flatMap .recover .recoverWith .fallbackTo .zip
.transform .onComplete .foreach
Future.successful .failed Future.sequence Future.traverse
```

### `String` / `StringOps`
```scala
.length .isEmpty .nonEmpty .startsWith .endsWith .contains
.toLowerCase .toUpperCase .trim .strip .reverse
.split .mkString .substring .slice .take .drop
.toInt .toIntOption .toDouble .toDoubleOption
.map .filter .foreach   (yes — String is a Seq[Char])
```

---

## You're done

If you've read this far and the cheatsheet feels familiar, you've absorbed the working vocabulary of a productive Scala engineer. The next step isn't reading more — it's writing real code, reading library source, and contributing back.

Recommended onward path:

- Build something with **Cats Effect** end-to-end. A small HTTP service + DB + Kafka producer is plenty to internalize the patterns.
- Read the source of one library you use: **Cats**, **Circe**, or **Doobie**. You'll see every concept from this guide applied for real.
- Tackle the ScalaCheck "[Property-Based Testing](https://www.scalacheck.org/)" exercises — they're a great way to deepen your understanding of generic functions.
- For language-design taste, watch **Martin Odersky's** Scala 3 talks on YouTube. His framing of contextual abstractions is the best reference for "why Scala 3."

This guide will continue to evolve. PRs welcome on the repo.

---

[← Previous: Chapter 25 — Best Practices](25-best-practices.md) | [Back to README](README.md)
