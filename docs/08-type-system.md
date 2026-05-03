# Chapter 8 — Type System Fundamentals

> Scala has one of the most expressive type systems in mainstream use. This chapter covers the foundational pieces: the type lattice, variance, bounds, and the tools for naming and asserting types.

**In this chapter:**
1. [The full type hierarchy](#1-the-full-type-hierarchy)
2. [Type aliases](#2-type-aliases)
3. [Variance: `+T`, `-T`, invariant](#3-variance-t--t-invariant)
4. [Upper and lower bounds](#4-upper-and-lower-bounds)
5. [View bounds (deprecated)](#5-view-bounds-deprecated)
6. [Context bounds](#6-context-bounds)
7. [Type ascription vs casting](#7-type-ascription-vs-casting)
8. [Scala 3 additions: opaque types, union, intersection](#8-scala-3-additions-opaque-types-union-intersection)

---

## 1. The full type hierarchy

```
                            Any
                           /   \
                      AnyVal    AnyRef  (= java.lang.Object)
                       / | \      | \
              Int Double Boolean  String, your classes...
                  Char Long ...
                       \   /
                       Null  (subtype of every AnyRef)
                         |
                     Nothing  (subtype of every type)
```

- **`Any`** — root, `==`, `equals`, `hashCode`, `toString`.
- **`AnyVal`** — value types: `Int`, `Long`, `Double`, `Float`, `Boolean`, `Char`, `Byte`, `Short`, `Unit`. Map to JVM primitives when possible.
- **`AnyRef`** — reference types: `String`, your classes, collections. Same as `java.lang.Object`.
- **`Null`** — type of `null`. Subtype of every `AnyRef`.
- **`Nothing`** — subtype of every type. No values. Used as the type of throws and infinite loops.

These appear constantly in inference results:

```scala
val xs = List()                  // List[Nothing]
val ys: List[Int] = xs           // OK: List[Nothing] <: List[Int] thanks to covariance
def fail(): Nothing = throw new Exception
val n: Int = if (cond) 1 else fail()    // works because Nothing <: Int
```

> **Scala 3 note:** Scala 3 supports *explicit nulls* (opt-in with `-Yexplicit-nulls`), making `Null` no longer a subtype of `String`/`AnyRef`. You'd have to write `String | Null` to allow null. Strongly recommended for new code.

---

## 2. Type aliases

`type` introduces a name for a type:

```scala
type UserId = String
type Result[A] = Either[String, A]

def find(id: UserId): Result[User] = ???
```

Aliases are *purely cosmetic* — `UserId` and `String` are interchangeable. They document intent but don't enforce it. For real type safety, use `case class UserId(value: String)` or Scala 3's `opaque type` (last section).

Aliases can take type parameters and have bounds:

```scala
type StringMap[V] = Map[String, V]
type Comparable[A] = A with java.lang.Comparable[A]
```

You can declare type aliases inside classes/traits/objects, scoping them like methods:

```scala
class Repo {
  type EntityId = Long
  def find(id: EntityId): Option[Entity] = ???
}
```

---

## 3. Variance: `+T`, `-T`, invariant

The big question: if `Dog <: Animal`, is `List[Dog] <: List[Animal]`?

The answer depends on how `List` is declared. Three options:

```scala
class Box[A]      // invariant   — Box[Dog] is NOT a Box[Animal]
class Box[+A]     // covariant   — Box[Dog]  IS  a Box[Animal]
class Box[-A]     // contravariant — Box[Animal] IS a Box[Dog]  (yes, that direction)
```

**Covariance (`+A`)** is correct when the type parameter only appears in *output* positions (return types, fields you read from). `List[+A]` is covariant because you read elements out; you never write into a `List`.

**Contravariance (`-A`)** is correct when the type parameter only appears in *input* positions. `Function1[-A, +B]` — a function from `Animal` can be used where a function from `Dog` is expected, because `Dog` is a kind of `Animal`. (Read: a "feeder of Animals" can feed Dogs.)

**Invariance** is the safe default — when the parameter appears in both input and output positions.

```scala
trait Producer[+A] {
  def get(): A                     // OK: A in output only
}

trait Consumer[-A] {
  def consume(a: A): Unit          // OK: A in input only
}

trait Mutable[A] {
  def get(): A
  def set(a: A): Unit              // both — must be invariant
}
```

The compiler enforces this. Try to declare `class Box[+A] { def set(a: A): Unit }` and you'll get a "covariant type A occurs in contravariant position" error.

### Why this matters in practice

- `List[+A]`, `Option[+A]`, `Vector[+A]` — covariant. So `List[Dog]` flows where `List[Animal]` is expected.
- `Set[A]`, `Map[K, +V]` — `Set` is invariant, `Map` is invariant in its key but covariant in its value.
- `Function[-A, +B]` — contravariant in input, covariant in output.

**Your custom collections / containers should follow this discipline.** It's free composability.

### Use-site variance with `<:` and `>:`

When you have an invariant type but want one-shot covariant behavior, you can declare it on the *use*:

```scala
def addAll[A, B <: A](xs: List[A], ys: List[B]): List[A] = xs ++ ys
```

Here, `B <: A` says "ys can have a more specific element type than xs" — temporary covariance.

---

## 4. Upper and lower bounds

Type parameters can be constrained:

- **Upper bound**: `A <: B` means "A must be a subtype of B."
- **Lower bound**: `A >: B` means "A must be a supertype of B."

```scala
def biggest[A <: Comparable[A]](xs: List[A]): A =
  xs.reduce((a, b) => if (a.compareTo(b) > 0) a else b)

biggest(List(3, 1, 4))    // 4
biggest(List("a", "c", "b"))  // "c"
```

`A <: Comparable[A]` (F-bounded polymorphism — see [Chapter 9](09-advanced-types.md)) constrains A to types that can compare against themselves.

Lower bounds show up in covariant collections:

```scala
class List[+A] {
  def prepend[B >: A](b: B): List[B]    // B >: A allows widening
}

val ints: List[Int] = List(1, 2, 3)
val anys: List[Any] = ints.prepend("hello")    // B = Any, broader than Int
```

The lower bound is essential for soundness: prepending a `String` to a `List[Int]` returns a `List[Any]` (the LUB), not a `List[Int]` that would be incorrect.

---

## 5. View bounds (deprecated)

`A <% B` meant "A *or implicitly convertible to* B." Removed in Scala 3 and deprecated in Scala 2.13. Replace with an explicit implicit conversion or context bound.

```scala
// old:
def maxOf[A <% Ordered[A]](a: A, b: A): A = if (a > b) a else b

// new (Scala 2.13+):
def maxOf[A](a: A, b: A)(implicit ord: Ordering[A]): A =
  if (ord.gt(a, b)) a else b

// or with context bound:
def maxOf[A: Ordering](a: A, b: A): A = {
  val ord = implicitly[Ordering[A]]
  if (ord.gt(a, b)) a else b
}
```

---

## 6. Context bounds

`A : F` is a shorthand for `A` plus an implicit `F[A]` parameter:

```scala
def show[A: Show](a: A): String = implicitly[Show[A]].show(a)

// equivalent to:
def show[A](a: A)(implicit ev: Show[A]): String = ev.show(a)
```

You'll see this everywhere in FP-flavored Scala (Cats, ZIO):

```scala
def doStuff[F[_]: Monad](f: F[Int]): F[String] = ???
def add[A: Numeric](a: A, b: A): A = implicitly[Numeric[A]].plus(a, b)
```

In Scala 2 the access pattern is `implicitly[F[A]]`. Scala 3 uses `summon[F[A]]` or just the lookup-by-type via `using`.

Context bounds are how the **type class pattern** is encoded ergonomically — covered in [Chapter 16](16-typeclasses.md).

---

## 7. Type ascription vs casting

**Type ascription** asks the compiler to treat an expression as a particular type. It's static; if it doesn't fit, you get a compile error.

```scala
val x = 1: Int                   // ascription
val ys = List(1, 2, 3): Seq[Int] // widen to Seq

val it: Iterable[Int] = List(1, 2, 3)  // assignment also ascribes
```

A common idiom: ascribing widens a result for type inference downstream:

```scala
val xs = List(1, 2, 3)
val ys = xs.foldLeft(List.empty[Int])((acc, x) => x :: acc)
```

The `List.empty[Int]` is ascription that prevents `acc` from being inferred as `List[Nothing]`.

**Casting** is `asInstanceOf`. It's runtime — it can throw `ClassCastException` if wrong:

```scala
val x: Any = "hello"
val s = x.asInstanceOf[String]    // works
val n = x.asInstanceOf[Int]       // throws at runtime
```

`isInstanceOf[T]` tests if an object is of type T at runtime:

```scala
x.isInstanceOf[String]    // true
```

Casts are a code smell. If you find yourself writing `asInstanceOf`, you've probably modelled something with `Any` that should be a sealed trait.

---

## 8. Scala 3 additions: opaque types, union, intersection

### Opaque types

Strongly-typed wrappers with **zero runtime cost**:

```scala
// scala 3
opaque type UserId = String

object UserId:
  def apply(s: String): UserId = s
  extension (id: UserId) def value: String = id

val id = UserId("u-42")
val s: String = id        // compile error! UserId is not a String outside its module
val s2: String = id.value // OK
```

Opaque types compile away — at runtime they're just `String`s — but the compiler treats them as distinct. Best of both worlds vs `case class UserId(value: String)` (which adds an allocation).

In Scala 2, you can approximate with `value class` (`extends AnyVal`), but it has more rules and corner cases.

### Union types

```scala
// scala 3
def parse(input: String | Int): Int = input match
  case s: String => s.toInt
  case n: Int    => n
```

`A | B` is a type that's either A or B. In Scala 2 you'd use `Either[A, B]` or a sealed trait.

### Intersection types

```scala
// scala 3
trait Resettable:
  def reset(): Unit

trait Growable:
  def add(item: String): Unit

def process(x: Resettable & Growable): Unit =
  x.reset()
  x.add("item")
```

`A & B` is "has both A and B."  In Scala 2 you'd use `with`:

```scala
// scala 2
def process(x: Resettable with Growable): Unit = ???
```

### Match types

```scala
// scala 3
type Elem[X] = X match
  case String => Char
  case Array[t] => t
  case Iterable[t] => t

val c: Elem["hello"] = 'h'
```

A type-level match. Power-user feature; useful in libraries like Tapir or lightweight Spark replacements.

---

## What you should now know

- The full Scala type hierarchy and what each piece is for.
- Type aliases — what they do and don't do.
- Covariance, contravariance, invariance, and how to choose.
- Upper and lower bounds, and where lower bounds appear in covariant types.
- Context bounds (`A : Show`) as the ergonomic form of implicit parameters.
- Type ascription vs casting and why casts are usually wrong.
- Scala 3's additions: opaque types (free wrappers), union, intersection, match types.

The next chapter ([Chapter 9 — Generics and Advanced Types](09-advanced-types.md)) goes deeper: F-bounded polymorphism, higher-kinded types, type lambdas, path-dependent types.

---

[← Previous: Chapter 7 — Traits](07-traits-and-inheritance.md) | [Back to README](README.md) | [Next: Chapter 9 — Advanced Types →](09-advanced-types.md)
