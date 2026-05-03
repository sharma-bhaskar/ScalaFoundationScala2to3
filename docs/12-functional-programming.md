# Chapter 12 — Functional Programming Foundations

> Scala isn't a "pure FP" language by force, but it's designed to make FP comfortable. This chapter covers the core ideas — purity, immutability, recursion, composition, ADTs — that everything later builds on.

**In this chapter:**
1. [Pure functions and referential transparency](#1-pure-functions-and-referential-transparency)
2. [Immutability in practice](#2-immutability-in-practice)
3. [First-class functions (recap)](#3-first-class-functions-recap)
4. [Recursion and tail recursion](#4-recursion-and-tail-recursion)
5. [Function composition](#5-function-composition)
6. [Currying revisited](#6-currying-revisited)
7. [Algebraic Data Types (ADTs)](#7-algebraic-data-types-adts)

---

## 1. Pure functions and referential transparency

A **pure function**:

- Returns the same output for the same inputs.
- Has no side effects (no mutation, no I/O, no exceptions, no global state).

```scala
// pure
def add(a: Int, b: Int): Int = a + b

// impure — depends on time
def now(): Long = System.currentTimeMillis()

// impure — mutates state
var counter = 0
def increment(): Int = { counter += 1; counter }

// impure — performs I/O
def greet(name: String): Unit = println(s"hello $name")

// impure — throws
def divide(a: Int, b: Int): Int = a / b   // throws on b == 0
```

A program built from pure functions is **referentially transparent**: you can replace any expression with its value without changing meaning.

```scala
val x = add(2, 3)
val y = add(2, 3)
// can be rewritten as:
val v = 5
val x = v
val y = v
```

Why care? Three concrete benefits:

- **Local reasoning** — read a function in isolation, ignore the rest of the program.
- **Easy testing** — no mocking, no setup; just inputs in, outputs out.
- **Easy parallelization** — pure code is automatically thread-safe.

In Scala you don't have to be 100% pure (and shouldn't try). The goal is to push impurity to the edges and keep the core pure.

---

## 2. Immutability in practice

```scala
// immutable
case class Point(x: Int, y: Int) {
  def shift(dx: Int, dy: Int): Point = Point(x + dx, y + dy)
}

val p1 = Point(0, 0)
val p2 = p1.shift(1, 2)    // new Point; p1 unchanged
```

The `case class` + `copy` pattern is the standard way to "modify" data:

```scala
case class User(name: String, age: Int, email: String)

val u1 = User("Bhaskar", 30, "x@y.com")
val u2 = u1.copy(age = 31)              // new User, age changed
val u3 = u1.copy(name = "Alice", age = 25)
```

For deeply nested structures, manual copy gets verbose:

```scala
case class Address(city: String, zip: String)
case class Profile(name: String, addr: Address)

// updating profile.addr.city by hand:
val newProfile = profile.copy(addr = profile.addr.copy(city = "Bangalore"))
```

For deep updates, **Monocle** (Scala lenses library) is the standard tool:

```scala
import monocle.macros.GenLens
val cityLens = GenLens[Profile](_.addr.city)

val updated = cityLens.replace("Bangalore")(profile)
```

Lenses give you composable accessors and modifiers for nested data. Worth learning if you have lots of nested case-class updates.

---

## 3. First-class functions (recap)

You already met them in [Chapter 2](02-functions-and-methods.md). The FP perspective:

```scala
val ops: Map[String, (Int, Int) => Int] = Map(
  "+" -> ((a, b) => a + b),
  "-" -> ((a, b) => a - b),
  "*" -> ((a, b) => a * b)
)

ops("+")(2, 3)    // 5
```

Functions go in collections, get passed around, returned, partially applied. This is the foundation of every higher-level FP construct.

---

## 4. Recursion and tail recursion

In FP, loops are usually recursion. But naive recursion eats stack:

```scala
def sum(xs: List[Int]): Int = xs match {
  case Nil       => 0
  case h :: rest => h + sum(rest)    // NOT tail recursive
}

sum(List.fill(1_000_000)(1))   // StackOverflowError
```

The recursive call isn't the *last thing* — we add `h` to its result. The compiler can't reuse the stack frame, so each call grows the stack.

Make it **tail-recursive**:

```scala
import scala.annotation.tailrec

def sum(xs: List[Int]): Int = {
  @tailrec
  def loop(rem: List[Int], acc: Int): Int = rem match {
    case Nil       => acc
    case h :: rest => loop(rest, acc + h)    // last thing is the call
  }
  loop(xs, 0)
}

sum(List.fill(1_000_000)(1))   // 1000000 — no stack issue
```

The `@tailrec` annotation tells the compiler "verify this is tail recursive; error otherwise." It's not optional discipline — without `@tailrec`, you might accidentally write something the compiler *can't* optimize and not notice.

The compiler turns tail-recursive methods into loops at the bytecode level. Same speed as `while`, with cleaner code.

**Caveats:**
- `@tailrec` only works for *direct* recursion (a method calls itself). Mutual recursion (`a` calls `b` calls `a`) doesn't get optimized — use **trampolining** (a library helper) if needed.
- Some recursive structures (`Tree`s, etc.) can't be made tail-recursive without restructuring.

For collection-shaped operations, `foldLeft` is implicitly tail-recursive:

```scala
def sum(xs: List[Int]): Int = xs.foldLeft(0)(_ + _)
```

Reach for `foldLeft`/`foldRight` first; write `@tailrec` when fold doesn't fit.

---

## 5. Function composition

Combine two functions into one. Two operators:

```scala
val incr: Int => Int = _ + 1
val double: Int => Int = _ * 2

val incrThenDouble: Int => Int = incr andThen double
val doubleThenIncr: Int => Int = incr compose double

incrThenDouble(3)    // (3 + 1) * 2 = 8
doubleThenIncr(3)    // (3 * 2) + 1 = 7
```

`f andThen g` means "do f, then g." `f compose g` means "do g, then f." Mathematicians: `(f compose g)(x) == f(g(x))`.

Composition lets you build pipelines from small named pieces:

```scala
val parse: String => Option[Int]   = _.toIntOption
val incr: Int => Int               = _ + 1
val format: Int => String          = n => f"$n%05d"

val pipeline: String => Option[String] =
  parse andThen (_.map(incr)) andThen (_.map(format))

pipeline("42")     // Some("00043")
pipeline("xyz")    // None
```

---

## 6. Currying revisited

We saw currying in [Chapter 2](02-functions-and-methods.md) as multiple parameter lists. The FP angle: currying is *the* way to make a multi-argument function feel like a chain of one-argument functions.

```scala
val add: Int => Int => Int = a => b => a + b

val plus5 = add(5)     // Int => Int
plus5(10)              // 15
```

Standard library:

```scala
def addCurried(a: Int)(b: Int): Int = a + b

addCurried(5) _    // Int => Int (in Scala 2; bare in Scala 3)
```

You also have `(_).curried` and `Function.uncurried`:

```scala
val f: (Int, Int) => Int = _ + _
val g: Int => Int => Int = f.curried
val h: (Int, Int) => Int = Function.uncurried(g)
```

In FP-flavored Scala (Cats, ZIO), curried APIs are everywhere — both for type inference and to enable partial application.

---

## 7. Algebraic Data Types (ADTs)

An ADT is a type built from two combinators:

- **Sum** — "this OR that." Encoded as a `sealed trait` with case subclasses, or as a Scala 3 `enum`.
- **Product** — "this AND that." Encoded as a `case class`.

Together they let you model domain data precisely.

### Sum types

```scala
// scala 2
sealed trait Result
case class Success(value: Int)   extends Result
case class Failure(error: String) extends Result

// scala 3
enum Result:
  case Success(value: Int)
  case Failure(error: String)
```

A `Result` is *either* a Success *or* a Failure. The compiler will tell you if your `match` misses a variant.

### Product types

```scala
case class Point(x: Int, y: Int)            // x AND y
case class Circle(center: Point, radius: Double)
case class User(name: String, age: Int, email: String)
```

A `User` is a name *and* an age *and* an email — all three fields together.

### Combining

ADTs compose:

```scala
sealed trait Tree[A]
case object Leaf                                      extends Tree[Nothing]
case class Node[A](left: Tree[A], v: A, right: Tree[A]) extends Tree[A]

def size[A](t: Tree[A]): Int = t match {
  case Leaf            => 0
  case Node(l, _, r)   => 1 + size(l) + size(r)
}
```

A `Tree` is *either* a Leaf, *or* a Node which has *left* and *value* and *right* — sum of products.

ADTs are how you model data in Scala. They give you:
- Exhaustive pattern matching.
- Clear shape — no nullable fields, no "valid only if".
- Free `equals`, `hashCode`, `toString`, `copy`, `unapply`.

The discipline: **whenever you have a "kind of X," reach for an ADT.** Don't use `String` enums or `Map` configurations for things with a finite, structured shape.

---

## What you should now know

- What "pure" and "referentially transparent" mean and why they matter.
- How to do "modification" through `copy` on case classes (and where lenses help).
- Tail recursion and `@tailrec` — how to write loops without stack overflow.
- Function composition with `andThen` and `compose`.
- ADTs as sums of products: the way to model data in Scala.

The next chapter ([Chapter 13 — Error Handling](13-error-handling.md)) takes the immutability/purity story to its natural extension — handling failure without exceptions.

---

[← Previous: Chapter 11 — Given/Using](11-given-using-scala3.md) | [Back to README](README.md) | [Next: Chapter 13 — Error Handling →](13-error-handling.md)
