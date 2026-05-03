# Chapter 16 — The Type Class Pattern

> Type classes are how Scala does ad-hoc polymorphism without inheritance. They're the foundation of Cats, ZIO, Doobie, Circe, and most serious FP libraries. This chapter walks the pattern from scratch and shows how to define your own.

**In this chapter:**
1. [The motivation: ad-hoc polymorphism](#1-the-motivation-ad-hoc-polymorphism)
2. [The pattern: trait + instances + summoner](#2-the-pattern-trait--instances--summoner)
3. [Worked example: `Show[A]`](#3-worked-example-showa)
4. [Worked example: `Eq[A]`](#4-worked-example-eqa)
5. [Worked example: `Ord[A]`](#5-worked-example-orda)
6. [Conditional / derived instances](#6-conditional--derived-instances)
7. [Type class derivation](#7-type-class-derivation)
8. [Coherence and orphans](#8-coherence-and-orphans)

---

## 1. The motivation: ad-hoc polymorphism

You want a `show` function that works for any type — `Int`, `String`, your case classes — without requiring those types to extend a common base.

Inheritance can't do it: you'd have to modify every type to extend `Showable`. Worse, you can't extend types you don't own (`Int`, `String`).

The type class pattern decouples "the operation" from "the type":

- Define an *interface* (the type class) parameterized by the type.
- Provide *instances* — implementations for each type.
- Look the right instance up at the call site, automatically.

This gives you **open extensibility**: anyone, anywhere, can add a `Show` instance for any type.

---

## 2. The pattern: trait + instances + summoner

Three pieces:

```scala
// 1. The trait — the interface
trait Show[A] {
  def show(a: A): String
}

// 2. The instances — implementations for specific types
object Show {
  implicit val intShow: Show[Int] = (n: Int) => n.toString
  implicit val strShow: Show[String] = (s: String) => s""""$s""""

  // 3. The summoner — convenience to fetch an instance
  def apply[A](implicit s: Show[A]): Show[A] = s
}
```

Use it:

```scala
def render[A: Show](a: A): String = Show[A].show(a)

render(42)         // "42"
render("hello")    // ""hello""
```

What just happened:

- `[A: Show]` is a context bound — the compiler adds an implicit parameter `(implicit ev: Show[A])`.
- `Show[A]` (the summoner call) looks up the implicit instance from scope.
- The implicit instance for `Int` was defined in `Show`'s companion, so it's automatically in scope.

> **Scala 3 note:** Same idea, cleaner syntax:
> ```scala
> // scala 3
> trait Show[A]:
>   def show(a: A): String
>
> object Show:
>   given Show[Int] with
>     def show(n: Int): String = n.toString
>   def apply[A](using s: Show[A]): Show[A] = s
>
> def render[A: Show](a: A): String = Show[A].show(a)
> ```

---

## 3. Worked example: `Show[A]`

A complete `Show` type class:

```scala
trait Show[A] {
  def show(a: A): String
}

object Show {
  // summoner
  def apply[A](implicit s: Show[A]): Show[A] = s

  // build an instance from a function
  def from[A](f: A => String): Show[A] = (a: A) => f(a)

  // primitive instances
  implicit val intShow: Show[Int]       = from(_.toString)
  implicit val longShow: Show[Long]     = from(_.toString)
  implicit val strShow: Show[String]    = from(s => s""""$s"""")
  implicit val boolShow: Show[Boolean]  = from(_.toString)

  // syntax helper — adds a `.show` method to any A with a Show instance
  implicit class ShowOps[A](a: A)(implicit s: Show[A]) {
    def show: String = s.show(a)
  }
}

// usage:
import Show._

42.show              // "42"
"hello".show         // ""hello""
true.show            // "true"
```

The `implicit class ShowOps` (or `extension` in Scala 3) is what gives you the lovely `42.show` syntax. This is a recurring pattern across FP libraries.

---

## 4. Worked example: `Eq[A]`

Equality decoupled from `Object.equals`:

```scala
trait Eq[A] {
  def eqv(a: A, b: A): Boolean
}

object Eq {
  def apply[A](implicit e: Eq[A]): Eq[A] = e
  def from[A](f: (A, A) => Boolean): Eq[A] = (a, b) => f(a, b)

  implicit val intEq: Eq[Int] = from(_ == _)
  implicit val strEq: Eq[String] = from(_ == _)

  implicit class EqOps[A](a: A)(implicit e: Eq[A]) {
    def ===(b: A): Boolean = e.eqv(a, b)
    def =!=(b: A): Boolean = !e.eqv(a, b)
  }
}

import Eq._

42 === 42        // true
"a" === "b"      // false
// 42 === "x"    // compile error: no Eq[String] for Int
```

The win over Java's `equals`: **type-safe** equality. `42.equals("42")` compiles in Java (and is silently wrong); `42 === "42"` won't compile here. Cats's `Eq` is exactly this idea.

---

## 5. Worked example: `Ord[A]`

Total ordering:

```scala
trait Ord[A] extends Eq[A] {
  def compare(a: A, b: A): Int      // negative, zero, or positive
  def lt(a: A, b: A): Boolean = compare(a, b) < 0
  def gt(a: A, b: A): Boolean = compare(a, b) > 0
  def eqv(a: A, b: A): Boolean = compare(a, b) == 0   // inherited from Eq
}

object Ord {
  def apply[A](implicit o: Ord[A]): Ord[A] = o
  def from[A](f: (A, A) => Int): Ord[A] = new Ord[A] {
    def compare(a: A, b: A): Int = f(a, b)
  }

  implicit val intOrd: Ord[Int] = from(_ - _)
  implicit val strOrd: Ord[String] = from(_.compareTo(_))
}

def maxOf[A: Ord](a: A, b: A): A =
  if (Ord[A].gt(a, b)) a else b

maxOf(3, 7)      // 7
maxOf("a", "z")  // "z"
```

This is the same pattern, with a richer interface. The standard library actually provides `scala.math.Ordering[A]` doing this exact job.

---

## 6. Conditional / derived instances

The real power: instances that depend on *other* instances.

```scala
implicit def listShow[A](implicit a: Show[A]): Show[List[A]] =
  Show.from(xs => xs.map(a.show).mkString("[", ", ", "]"))

implicit def optionShow[A](implicit a: Show[A]): Show[Option[A]] =
  Show.from {
    case Some(v) => s"Some(${a.show(v)})"
    case None    => "None"
  }

implicit def tupleShow[A, B](implicit a: Show[A], b: Show[B]): Show[(A, B)] =
  Show.from { case (x, y) => s"(${a.show(x)}, ${b.show(y)})" }
```

Now:

```scala
List(1, 2, 3).show                       // "[1, 2, 3]"
Some("hi").show                          // "Some("hi")"
(42, "answer").show                      // "(42, "answer")"
List(Some(1), None, Some(3)).show        // "[Some(1), None, Some(3)]"
```

The compiler chains the instances automatically. `List[Option[Int]]` works because:
- `Show[List[A]]` exists if `Show[A]` does.
- `Show[Option[A]]` exists if `Show[A]` does.
- `Show[Int]` exists.

This is **type-class composition**, and it's the magic that makes libraries like Circe (JSON), Doobie (DB), and Cats so productive.

---

## 7. Type class derivation

Even better: have the compiler *derive* instances for case classes automatically.

### Manual derivation per case class

For one type:

```scala
case class Person(name: String, age: Int)

object Person {
  implicit val show: Show[Person] = Show.from(p =>
    s"Person(name=${p.name.show}, age=${p.age.show})"
  )
}
```

Verbose. For many types, automate it.

### Generic derivation with **Shapeless** (Scala 2)

Shapeless reflects case classes into a generic representation (`HList` of fields), and you write a Show instance once for the generic shape:

```scala
import shapeless._

implicit val hnilShow: Show[HNil] = Show.from(_ => "")

implicit def hlistShow[H, T <: HList](
  implicit h: Show[H], t: Show[T]
): Show[H :: T] =
  Show.from { case head :: tail =>
    val tailStr = t.show(tail)
    if (tailStr.isEmpty) h.show(head) else s"${h.show(head)}, $tailStr"
  }

implicit def genericShow[A, R](
  implicit gen: Generic.Aux[A, R], r: Show[R]
): Show[A] =
  Show.from(a => r.show(gen.to(a)))

// now any case class gets a Show automatically:
Person("Bhaskar", 30).show
```

The mechanics are dense; in practice, libraries like **Magnolia** (works on Scala 2 and 3) wrap this for you.

### Native `derives` (Scala 3)

Scala 3 makes derivation a language feature:

```scala
// scala 3
trait Show[A]:
  def show(a: A): String
object Show:
  given Show[Int] with
    def show(n: Int): String = n.toString
  given Show[String] with
    def show(s: String): String = s""""$s""""

  inline given derived[A](using m: scala.deriving.Mirror.Of[A]): Show[A] = ???
  // (real implementations use Mirror to inspect fields)

case class Person(name: String, age: Int) derives Show

Person("Bhaskar", 30).show    // works
```

The `derives Show` on the case class triggers the derivation machinery. Way cleaner than Scala 2.

---

## 8. Coherence and orphans

A type class is **coherent** when there's exactly one instance of each `TypeClass[A]` everywhere. Scala doesn't enforce this — you *could* have two `Show[Int]` instances in different scopes and pick one or the other. That's a bug waiting to happen.

An **orphan instance** is one defined outside both the type's package and the type class's package. They're allowed in Scala, but discouraged because they're invisible to anyone who doesn't import the right thing.

The good rule: **instances live in the companion object of the type, or the companion of the type class.** That makes them discoverable without imports and enforces coherence (only one place defines them).

If you absolutely must have an orphan:

- Document it.
- Put all orphans for a type in one obvious place (a `MyAppInstances` object).
- Make users opt in with an explicit import.

Cats's strict-coherence rule is: *the only legal place to define an instance is in the companion of the type or the companion of the type class.* Following it removes a class of bugs.

---

## What you should now know

- Why type classes are how Scala does open polymorphism without inheritance.
- The three-piece pattern: trait, instance, summoner.
- How to build `Show`, `Eq`, `Ord` from scratch.
- Conditional/derived instances — `Show[List[A]]` from `Show[A]`.
- Derivation — manually, with Shapeless/Magnolia, and natively in Scala 3 via `derives`.
- Coherence and the orphan-instance rule.

The next chapter ([Chapter 17 — Functors, Applicatives, Monads](17-monads-and-fp-abstractions.md)) introduces the most important type classes in functional programming.

---

[← Previous: Chapter 15 — For Comprehensions](15-for-comprehensions.md) | [Back to README](README.md) | [Next: Chapter 17 — Functors / Applicatives / Monads →](17-monads-and-fp-abstractions.md)
