# Chapter 17 — Functors, Applicatives, Monads, and Friends

> The big-name FP type classes. They look intimidating but each is a tiny interface with two or three methods. This chapter explains what each means, the laws they obey, and why they're useful in everyday code.

**In this chapter:**
1. [`Semigroup` and `Monoid`](#1-semigroup-and-monoid)
2. [`Functor`](#2-functor)
3. [`Applicative`](#3-applicative)
4. [`Monad`](#4-monad)
5. [`Traverse` and `Foldable`](#5-traverse-and-foldable)
6. [Why monads don't compose; transformers](#6-why-monads-dont-compose-transformers)
7. [A brief tour of Cats](#7-a-brief-tour-of-cats)

---

## 1. `Semigroup` and `Monoid`

The simplest of the bunch. A **semigroup** has an associative binary operation:

```scala
trait Semigroup[A] {
  def combine(a: A, b: A): A     // associative: combine(a, combine(b, c)) == combine(combine(a, b), c)
}
```

Examples: `Int` with `+`, `String` with `++`, `List[A]` with `:::`.

```scala
implicit val intSum: Semigroup[Int] = (a, b) => a + b
implicit val strConcat: Semigroup[String] = (a, b) => a + b
```

A **monoid** is a semigroup with an identity element (a value `empty` such that `combine(empty, x) == x`):

```scala
trait Monoid[A] extends Semigroup[A] {
  def empty: A
}

implicit val intSum: Monoid[Int] = new Monoid[Int] {
  def empty: Int = 0
  def combine(a: Int, b: Int): Int = a + b
}

implicit val strConcat: Monoid[String] = new Monoid[String] {
  def empty: String = ""
  def combine(a: String, b: String): String = a + b
}
```

Why care? It lets you write generic "fold a sequence into one thing":

```scala
def combineAll[A: Monoid](xs: List[A]): A = {
  val m = implicitly[Monoid[A]]
  xs.foldLeft(m.empty)(m.combine)
}

combineAll(List(1, 2, 3, 4))      // 10
combineAll(List("a", "b", "c"))   // "abc"
combineAll(List(List(1), List(2,3), List(4)))   // List(1, 2, 3, 4)
```

A monoid is one of the most reusable abstractions you'll meet — it's everywhere from word-counting to log accumulation to building HTML.

---

## 2. `Functor`

A `Functor[F[_]]` is anything you can `map` over:

```scala
trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}
```

Examples: `Option`, `List`, `Either[E, *]`, `Future`, `Try`, your custom monads.

```scala
implicit val optionFunctor: Functor[Option] = new Functor[Option] {
  def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa match {
    case Some(a) => Some(f(a))
    case None    => None
  }
}
```

The **functor laws**:

- **Identity**: `map(fa)(x => x) == fa`
- **Composition**: `map(map(fa)(f))(g) == map(fa)(f andThen g)`

These are intuitive; they say "map shouldn't add weird behavior."

The use of an abstract `Functor`: write code that works for any container.

```scala
def increment[F[_]: Functor](fa: F[Int]): F[Int] =
  implicitly[Functor[F]].map(fa)(_ + 1)

increment(Option(5))    // Some(6)
increment(List(1, 2))   // List(2, 3)
```

---

## 3. `Applicative`

A `Functor` that can also lift values into `F` and combine independent `F`s:

```scala
trait Applicative[F[_]] extends Functor[F] {
  def pure[A](a: A): F[A]
  def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]    // apply a wrapped function

  // derived:
  def map2[A, B, C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
    ap(map(fa)(a => (b: B) => f(a, b)))(fb)
}
```

Why useful? **Independent computations, combined.**

```scala
val opt1: Option[Int] = Some(2)
val opt2: Option[Int] = Some(3)

implicitly[Applicative[Option]].map2(opt1, opt2)(_ + _)   // Some(5)
```

In Cats, the syntax is much nicer:

```scala
import cats.implicits._
(opt1, opt2).mapN(_ + _)    // Some(5)
(Some(1), Some(2), Some(3)).mapN(_ + _ + _)   // Some(6)
```

The crucial difference vs `Monad`: with `Applicative`, the computations don't depend on each other and can run **in parallel**. Real Future-flavored example:

```scala
val fa: Future[Int] = ...
val fb: Future[Int] = ...

(fa, fb).mapN(_ + _)    // both run in parallel; result is a Future[Int]
```

Compare to a `for` over Futures, which runs them sequentially. `Applicative.mapN` is the "I want both results, in parallel" idiom.

---

## 4. `Monad`

The biggest of the bunch. An `Applicative` that adds **dependent sequencing**:

```scala
trait Monad[F[_]] extends Applicative[F] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}
```

The **monad laws**:

- **Left identity**: `flatMap(pure(a))(f) == f(a)`
- **Right identity**: `flatMap(fa)(pure) == fa`
- **Associativity**: `flatMap(flatMap(fa)(f))(g) == flatMap(fa)(a => flatMap(f(a))(g))`

What `flatMap` *means*: "use the result of one F to choose the next F." This is what makes monads **sequential**:

```scala
for {
  x <- fa
  y <- fbThatDependsOnX(x)    // y depends on x
} yield y
```

Every for-comprehension in Scala is implicitly a monadic chain. We covered this in [Chapter 15](15-for-comprehensions.md).

Generic monad code:

```scala
def safeDivide[F[_]: Monad](numerator: F[Int], denom: F[Int]): F[Int] = {
  val M = implicitly[Monad[F]]
  M.flatMap(numerator) { n =>
    M.flatMap(denom) { d =>
      if (d == 0) M.pure(0) else M.pure(n / d)
    }
  }
}

safeDivide(Some(10), Some(2))    // Some(5)
safeDivide(Some(10), None)       // None
```

The "Hello, world" of generic monad programming.

---

## 5. `Traverse` and `Foldable`

**`Foldable`** is "anything you can fold":

```scala
trait Foldable[F[_]] {
  def foldLeft[A, B](fa: F[A], b: B)(f: (B, A) => B): B
  def foldRight[A, B](fa: F[A], b: B)(f: (A, => B) => B): B
}
```

`List`, `Option`, `Vector`, etc., all have this. Foldable gives you generic `combineAll`, `find`, `forall`.

**`Traverse`** is one of the most useful FP abstractions: turn `F[G[A]]` into `G[F[A]]` (or run an effect over a structure):

```scala
trait Traverse[F[_]] extends Foldable[F] with Functor[F] {
  def traverse[G[_]: Applicative, A, B](fa: F[A])(f: A => G[B]): G[F[B]]
  def sequence[G[_]: Applicative, A](fga: F[G[A]]): G[F[A]] =
    traverse(fga)(identity)
}
```

In English: "given a list of effectful things, give me the effect of a list of things."

```scala
import cats.implicits._

val ids = List("1", "2", "3")
def parse(s: String): Option[Int] = s.toIntOption

ids.traverse(parse)    // Some(List(1, 2, 3))

val bad = List("1", "x", "3")
bad.traverse(parse)    // None  — first failure short-circuits
```

`traverse` over `List` + `Option` gives you "parse all of them; if any fails, the whole thing is None." This is the absolute everyday tool in real FP code:

```scala
val urls = List("/a", "/b", "/c")
def fetch(url: String): Future[Response] = ???

val responses: Future[List[Response]] = urls.traverse(fetch)
```

Compared to:

```scala
val responses: Future[List[Response]] = Future.sequence(urls.map(fetch))
```

`traverse` is the more general form. Cats provides it for any `Traverse` and any `Applicative`.

---

## 6. Why monads don't compose; transformers

A real-world example. You have a function returning `Future[Option[A]]` — async, may not find. The straightforward chain:

```scala
def step1(): Future[Option[Int]] = ???
def step2(n: Int): Future[Option[String]] = ???

// you want to combine them. for-comprehension over Future doesn't see Option:
for {
  optA <- step1()
  optB <- optA match {
    case Some(a) => step2(a)
    case None    => Future.successful(None)
  }
} yield optB
// awkward!
```

Monads don't compose: `Future[Option[A]]` isn't a "monad" — it's a Future-of-a-Monad. You can't just `flatMap` through both layers.

The fix: a **monad transformer**. `OptionT[F, A]` is "an `F[Option[A]]`" with monad operations that look through both:

```scala
import cats.data.OptionT
import cats.implicits._

val combined: OptionT[Future, String] = for {
  a <- OptionT(step1())
  b <- OptionT(step2(a))
} yield b

val eventualOption: Future[Option[String]] = combined.value
```

Inside the `for`, `a: Int` and `b: String` — the monad transformer hides both layers. Outside, you still have `Future[Option[String]]`.

Common transformers:
- `OptionT[F, A]` — F[Option[A]]
- `EitherT[F, E, A]` — F[Either[E, A]]
- `ReaderT[F, R, A]` — R => F[A] (a/k/a `Kleisli`)
- `StateT[F, S, A]` — S => F[(S, A)]

Cats provides them all. They're powerful but accumulate quickly. Many teams reach for an effect system (Cats Effect, ZIO) instead, which solves the same problem with one monad that does everything.

---

## 7. A brief tour of Cats

[Cats](https://typelevel.org/cats/) is the standard FP library for Scala. It provides:

- **Type classes**: `Semigroup`, `Monoid`, `Functor`, `Applicative`, `Monad`, `Traverse`, `Foldable`, `Eq`, `Order`, `Show`, and more.
- **Instances**: for stdlib types (`Option`, `Either`, `List`, ...) and from many other libraries.
- **Data types**: `NonEmptyList`, `NonEmptyMap`, `Validated`, `Ior`, `Chain`, monad transformers (`OptionT`, `EitherT`).
- **Syntax**: extension methods that make all of the above ergonomic (`.combine`, `.map`, `.traverse`, `===`, etc.).

Setup:

```scala
// build.sbt
libraryDependencies += "org.typelevel" %% "cats-core" % "2.10.0"
```

```scala
import cats._
import cats.implicits._

(Some(1), Some(2)).mapN(_ + _)              // Some(3)
List("1", "2", "x").traverse(_.toIntOption) // None
1.some                                       // Some(1)
"hello".asRight[String]                      // Either[String, String]
```

A typical Cats import set: `import cats._; import cats.implicits._` brings in everything. Some teams prefer narrower imports for readability.

If you do FP-flavored Scala in 2026, you're using Cats (or ZIO, which has a similar surface).

---

## What you should now know

- The pyramid of abstractions: `Semigroup` → `Monoid` → `Functor` → `Applicative` → `Monad`, plus `Traverse` and `Foldable`.
- The laws (briefly) and what they enforce.
- Why `Applicative.mapN` is parallel and `Monad.flatMap` is sequential.
- `traverse` as the everyday tool that turns `F[G[A]]` into `G[F[A]]`.
- Why monads don't compose and the role of monad transformers.
- A working knowledge of what the **Cats** library brings to the table.

The next chapter ([Chapter 18 — Futures](18-concurrency-futures.md)) brings concurrency back into focus — Scala's built-in `Future` API.

---

[← Previous: Chapter 16 — Type Classes](16-typeclasses.md) | [Back to README](README.md) | [Next: Chapter 18 — Futures →](18-concurrency-futures.md)
