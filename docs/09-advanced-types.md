# Chapter 9 — Generics and Advanced Types

> The mechanics of parameterized types, plus Scala's deeper type-system features: F-bounded polymorphism, higher-kinded types, type lambdas, path-dependent types, structural types, refined types.

**In this chapter:**
1. [Generic classes and methods](#1-generic-classes-and-methods)
2. [F-bounded polymorphism](#2-f-bounded-polymorphism)
3. [Higher-kinded types](#3-higher-kinded-types)
4. [Type lambdas](#4-type-lambdas)
5. [Path-dependent types](#5-path-dependent-types)
6. [Structural types](#6-structural-types)
7. [Refined types](#7-refined-types)
8. [Existential types (Scala 2 only)](#8-existential-types-scala-2-only)

---

## 1. Generic classes and methods

```scala
class Box[A](val value: A) {
  def map[B](f: A => B): Box[B] = new Box(f(value))
}

val intBox = new Box(42)
val strBox = intBox.map(_.toString)    // Box[String]

def first[A](xs: List[A]): Option[A] = xs.headOption
def pair[A, B](a: A, b: B): (A, B) = (a, b)

trait Container[A] {
  def get: A
  def put(a: A): Unit
}
```

Type parameters can have:
- Variance (`+A`, `-A`).
- Bounds (`A <: T`, `A >: T`).
- Context bounds (`A : Ord`).

```scala
def sort[A : Ordering](xs: List[A]): List[A] = xs.sorted
```

---

## 2. F-bounded polymorphism

When a type parameter refers to itself in its own bound:

```scala
trait Comparable[A <: Comparable[A]] {
  def compareTo(other: A): Int
}

class IntWrapper(val n: Int) extends Comparable[IntWrapper] {
  def compareTo(other: IntWrapper): Int = n - other.n
}
```

The pattern `A <: Comparable[A]` says "A is comparable *to itself*." It's how Java's `Comparable<T>` works under the hood.

Useful for self-typed APIs: methods that should return *the subclass's own type*, not the abstract one.

```scala
trait Builder[Self <: Builder[Self]] {
  def withName(n: String): Self
  def withAge(a: Int): Self
}

class PersonBuilder extends Builder[PersonBuilder] {
  def withName(n: String): PersonBuilder = this
  def withAge(a: Int): PersonBuilder = this
}

new PersonBuilder().withName("X").withAge(30)    // type is PersonBuilder, not Builder
```

In modern Scala, **type classes** (next chapter) usually replace F-bounded polymorphism for these cases.

---

## 3. Higher-kinded types

A *kind* is the "type of a type." `Int` has kind `*` (a regular type). `List` has kind `* -> *` — it takes a type and gives you a type. `Map` has kind `* -> * -> *`.

You can be polymorphic over higher-kinded types:

```scala
trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

object Functor {
  implicit val listFunctor: Functor[List] = new Functor[List] {
    def map[A, B](fa: List[A])(f: A => B): List[B] = fa.map(f)
  }

  implicit val optFunctor: Functor[Option] = new Functor[Option] {
    def map[A, B](fa: Option[A])(f: A => B): Option[B] = fa.map(f)
  }
}

def doubled[F[_]: Functor](fa: F[Int]): F[Int] =
  implicitly[Functor[F]].map(fa)(_ * 2)

doubled(List(1, 2, 3))     // List(2, 4, 6)
doubled(Option(5))          // Some(10)
```

`F[_]` is a higher-kinded type parameter: F is a type constructor that takes one type. `F[A]` then is a type. This is the foundation of category-theory-flavored libraries (Cats, Scalaz, ZIO).

For two-parameter type constructors (like `Either`, `Map`):

```scala
trait Bifunctor[F[_, _]] {
  def bimap[A, B, C, D](fab: F[A, B])(f: A => C, g: B => D): F[C, D]
}
```

---

## 4. Type lambdas

What if you want to use `Either` (kind `* -> * -> *`) where a `F[_]` (kind `* -> *`) is expected? You partially apply the type.

In Scala 2, this requires a clunky **type lambda**:

```scala
// scala 2
type EitherStringFunctor = Functor[({type L[A] = Either[String, A]})#L]
```

`({type L[A] = Either[String, A]})#L` is a structural type containing a type alias `L`, and `#L` selects it. It's ugly but it works.

> **Scala 3 note:** Scala 3 has clean syntax for this:
> ```scala
> // scala 3
> type EitherStringFunctor = Functor[[A] =>> Either[String, A]]
> ```
> `[A] =>> Either[String, A]` is a type lambda — anonymous, takes A, returns the type.

Libraries like **kind-projector** (compiler plugin) give Scala 2 this syntax:

```scala
// with kind-projector compiler plugin
type EitherStringFunctor = Functor[Either[String, *]]
```

If you're doing FP-flavored Scala 2, install kind-projector. Saves a lot of pain.

---

## 5. Path-dependent types

A type defined inside an instance can be *different per instance* — the path to that type is part of its identity.

```scala
class Outer {
  class Inner
  def make(): Inner = new Inner
}

val o1 = new Outer
val o2 = new Outer

val a: o1.Inner = o1.make()
val b: o2.Inner = o2.make()
// val c: o1.Inner = o2.make()    // compile error! different paths
```

`o1.Inner` and `o2.Inner` are *different types* even though both are `Outer#Inner`. The type's path includes which instance owns it.

`Outer#Inner` (the projection, with `#`) is the "any instance's Inner" type:

```scala
val any: Outer#Inner = o1.make()    // OK
```

This is rarely written explicitly, but it's how you sometimes see APIs like Akka's `ActorContext[T]#Self` or some database libraries that scope types to a session.

---

## 6. Structural types

Specify a type by what it *can do*, not by name. Effectively, duck typing in the type system.

```scala
def describe(x: { def name: String; def size: Int }): String =
  s"${x.name} (size ${x.size})"

class A {
  def name = "foo"
  def size = 100
}

describe(new A)    // "foo (size 100)"
```

Any object with the matching members fits. Behind the scenes, structural types use **reflection** to dispatch — they're slow. Avoid them in hot paths.

> **Scala 3 note:** Structural types are still supported but require importing `scala.reflect.Selectable.reflectiveSelectable` for reflective access, and there are alternative approaches with `Selectable`:
> ```scala
> // scala 3
> type Named = { def name: String }
> ```

For most "common shape" use cases, traits or type classes are better than structural types.

---

## 7. Refined types

A *refined type* adds constraints to a base type. `A with B { ... }` syntax adds member constraints:

```scala
type ResettableGrowable = scala.collection.mutable.Growable[Int] {
  def reset(): Unit
}
```

For value-level refinement (e.g., "an Int between 1 and 100"), Scala doesn't have built-in support, but the **refined** library does:

```scala
import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric._

val pos: Int Refined Positive = refineMV[Positive](42)
// val bad: Int Refined Positive = refineMV[Positive](-1)   // compile error!
```

Refined types are *compile-time* constraints. They're not runtime checks — that's `require(n > 0)`. They prevent illegal states from being representable.

---

## 8. Existential types (Scala 2 only)

A way to express "some unknown type." `forSome` syntax:

```scala
// scala 2
def someList(): List[T] forSome { type T <: Number } = ???
```

In practice, the wildcard `_` is sugar for the common case:

```scala
val xs: List[_]              // List[T] forSome { type T }
val xs: List[_ <: Number]    // List[T] forSome { type T <: Number }
```

You see existentials whenever you write `List[_]` or work with Java raw types.

> **Scala 3 note:** `forSome` is removed. Use wildcards (`List[?]` in Scala 3) or convert to a generic method.

---

## What you should now know

- Generic classes and methods, with bounds and context bounds.
- F-bounded polymorphism for self-referential types (and why type classes are usually better).
- Higher-kinded types — `F[_]` as a parameter — and how they enable Cats/ZIO-style abstraction.
- Type lambdas in Scala 2 (ugly), Scala 3 (clean), and via kind-projector.
- Path-dependent types and the `#` projection.
- Structural types — what they are and why to avoid them.
- Refined types and the **refined** library for compile-time value constraints.
- Existential types (legacy Scala 2) and their replacements.

The next chapter ([Chapter 10 — Implicits Scala 2](10-implicits-scala2.md)) opens the implicits chapter — one of Scala's most powerful and most contentious features.

---

[← Previous: Chapter 8 — Type System](08-type-system.md) | [Back to README](README.md) | [Next: Chapter 10 — Implicits →](10-implicits-scala2.md)
