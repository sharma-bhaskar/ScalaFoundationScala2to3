# Chapter 15 — For Comprehensions

> Scala's `for/yield` is sugar over `flatMap`, `map`, and `withFilter`. Once you see the desugaring, you understand how `for` works for *anything* with those methods — Options, Eithers, Futures, IOs, even your own types.

**In this chapter:**
1. [The simple cases](#1-the-simple-cases)
2. [Desugaring](#2-desugaring)
3. [Generators, guards, value definitions](#3-generators-guards-value-definitions)
4. [`for` over `Option`, `Either`, `Try`, `Future`](#4-for-over-option-either-try-future)
5. [Pattern matching in generators](#5-pattern-matching-in-generators)
6. [Building your own monad that works in `for`](#6-building-your-own-monad-that-works-in-for)
7. [Common pitfalls](#7-common-pitfalls)

---

## 1. The simple cases

Iterate and produce:

```scala
val squares = for (i <- 1 to 5) yield i * i
// Vector(1, 4, 9, 16, 25)
```

Multiple generators (Cartesian product):

```scala
for {
  x <- 1 to 3
  y <- 1 to 3
} yield (x, y)
// Vector((1,1), (1,2), (1,3), (2,1), (2,2), (2,3), (3,1), (3,2), (3,3))
```

With a guard:

```scala
for {
  x <- 1 to 10
  if x % 2 == 0
} yield x * x
// Vector(4, 16, 36, 64, 100)
```

Without `yield` (side-effecting loop):

```scala
for (x <- List(1, 2, 3)) println(x)
```

---

## 2. Desugaring

Every `for` desugars to chains of `flatMap`, `map`, and `withFilter`. Three rules:

**Rule 1**: a single generator with `yield` becomes `map`.

```scala
for (x <- xs) yield f(x)
//  desugars to:
xs.map(x => f(x))
```

**Rule 2**: multiple generators become nested `flatMap` + a final `map`.

```scala
for {
  x <- xs
  y <- ys
} yield (x, y)
//  desugars to:
xs.flatMap(x => ys.map(y => (x, y)))
```

**Rule 3**: guards become `withFilter`.

```scala
for {
  x <- xs
  if x > 0
} yield x
//  desugars to:
xs.withFilter(x => x > 0).map(x => x)
```

So this:

```scala
for {
  x <- xs
  y <- ys
  if pred(x, y)
} yield combine(x, y)
```

becomes:

```scala
xs.flatMap { x =>
  ys.withFilter(y => pred(x, y))
    .map(y => combine(x, y))
}
```

This desugaring is **why** `for` works on any type that has `map`, `flatMap`, and `withFilter`. The compiler doesn't know it's a `List` — it just sees that the methods exist.

---

## 3. Generators, guards, value definitions

Three constructs in a `for`:

```scala
for {
  x <- xs                  // generator
  if x > 0                 // guard
  y = x * 2                // value definition (no <- )
  z <- 1 to y              // another generator
} yield (x, y, z)
```

**Generator** (`x <- xs`) — pulls one value out of a "container" (like `flatMap`).

**Guard** (`if cond`) — filters; non-matching elements are skipped.

**Value definition** (`y = x * 2`) — a regular `val`, scoped through the rest of the comprehension. No `<-`.

---

## 4. `for` over `Option`, `Either`, `Try`, `Future`

Because all of these have `flatMap` and `map`, `for` works on them transparently:

### Option

```scala
def parseInt(s: String): Option[Int] = s.toIntOption

val sum: Option[Int] = for {
  a <- parseInt("10")
  b <- parseInt("20")
  c <- parseInt("30")
} yield a + b + c
// Some(60)

val bad: Option[Int] = for {
  a <- parseInt("10")
  b <- parseInt("xyz")    // None — short-circuits
  c <- parseInt("30")     // never runs
} yield a + b + c
// None
```

### Either

```scala
def parseInt(s: String): Either[String, Int] =
  s.toIntOption.toRight(s"not an int: $s")

val sum: Either[String, Int] = for {
  a <- parseInt("10")
  b <- parseInt("20")
} yield a + b
// Right(30)

val bad = for {
  a <- parseInt("10")
  b <- parseInt("xyz")    // Left, short-circuits
} yield a + b
// Left("not an int: xyz")
```

### Try

```scala
import scala.util.Try

val result: Try[Int] = for {
  a <- Try("10".toInt)
  b <- Try("20".toInt)
} yield a + b
// Success(30)
```

### Future

```scala
import scala.concurrent.{Future, ExecutionContext}
import ExecutionContext.Implicits.global

def fetch(url: String): Future[String] = ???

val combined: Future[String] = for {
  a <- fetch("/a")
  b <- fetch("/b")    // sequential — runs after a completes
  c <- fetch("/c")
} yield s"$a / $b / $c"
```

Note: `for` on `Future`s runs them **sequentially** — each one waits for the previous. For parallel, kick them off first:

```scala
val fa = fetch("/a")
val fb = fetch("/b")
val fc = fetch("/c")

val combined = for { a <- fa; b <- fb; c <- fc } yield s"$a / $b / $c"
// all three run in parallel; the for waits on each in order
```

---

## 5. Pattern matching in generators

The left side of `<-` can be a pattern:

```scala
val pairs = List((1, "a"), (2, "b"), (3, "c"))

for {
  (n, s) <- pairs
} yield s"$n:$s"
// List("1:a", "2:b", "3:c")
```

If the pattern is *partial* (might not match), non-matching elements are skipped — same as a guard:

```scala
val mixed: List[Any] = List(1, "two", 3, "four", 5)

for {
  case i: Int <- mixed       // Scala 3 syntax; Scala 2 omits `case`
} yield i * 10
// List(10, 30, 50)
```

> **Scala 3 note:** Refutable patterns require `case` keyword. Scala 2 didn't.

This is one reason `for` is useful beyond simple iteration — it handles "filter and transform" elegantly.

---

## 6. Building your own monad that works in `for`

Any type with `flatMap`, `map`, and (optionally) `withFilter` becomes `for`-comprehensible. Build your own:

```scala
case class Wrapped[A](value: A) {
  def map[B](f: A => B): Wrapped[B] = Wrapped(f(value))
  def flatMap[B](f: A => Wrapped[B]): Wrapped[B] = f(value)
  def withFilter(p: A => Boolean): Wrapped[A] =
    if (p(value)) this
    else throw new NoSuchElementException("filter failed")
}

val r = for {
  a <- Wrapped(10)
  b <- Wrapped(20)
} yield a + b
// Wrapped(30)
```

This is exactly how `Option`, `Either`, `Future`, `IO`, and any user-defined monad earn their `for/yield` support.

For real monads (with proper laws), see [Chapter 17](17-monads-and-fp-abstractions.md) — Cats's `Monad` type class formalizes this.

---

## 7. Common pitfalls

### Mixing types

The compiler infers `for` based on the *first* generator's type. You can't mix `Option` and `Either` in one `for`:

```scala
def maybe: Option[Int] = Some(1)
def err: Either[String, Int] = Right(2)

for {
  a <- maybe         // Option[Int]
  b <- err           // Either[String, Int]   — type mismatch!
} yield a + b
```

The fix: convert to the same shape first.

```scala
for {
  a <- maybe.toRight("missing")
  b <- err
} yield a + b
// Either[String, Int]
```

### Sequential vs parallel `Future`

As shown above, `for` over `Future`s is sequential. If you mean parallel, kick off the futures *before* the `for`:

```scala
// sequential
val seq = for {
  a <- fetch("/a")
  b <- fetch("/b")
} yield (a, b)

// parallel
val fa = fetch("/a")
val fb = fetch("/b")
val par = for { a <- fa; b <- fb } yield (a, b)
```

### Guards aren't free for all types

Some types don't define `withFilter`. For instance, `Future` doesn't have a useful `withFilter` (a future "matched" or "didn't match" doesn't really make sense). Guards in `for/Future` will compile but throw on filter failure — usually not what you want.

### Variable definitions need `=`, not `<-`

This is a bug:

```scala
for {
  x <- xs
  y <- x + 1     // wrong! tries to flatMap on Int (an actual error)
} yield y
```

The correct form:

```scala
for {
  x <- xs
  y = x + 1      // value definition
} yield y
```

---

## What you should now know

- The desugaring rules: `<-` → `flatMap`, `=` → val, `if` → `withFilter`, last `yield` → `map`.
- `for` works on `Option`, `Either`, `Try`, `Future`, and any custom monad.
- Patterns can appear on the left of `<-`, with refutable patterns acting as filters.
- How to make a custom type `for`-comprehensible by defining `flatMap`/`map`/`withFilter`.
- The "futures-in-a-for-are-sequential" gotcha and its fix.

The next chapter ([Chapter 16 — Type Classes](16-typeclasses.md)) introduces the type class pattern formally.

---

[← Previous: Chapter 14 — Lazy Evaluation](14-lazy-evaluation.md) | [Back to README](README.md) | [Next: Chapter 16 — Type Classes →](16-typeclasses.md)
