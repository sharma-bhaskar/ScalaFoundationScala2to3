# Chapter 11 — Given/Using (Scala 3)

> Scala 3 redesigned implicits into multiple focused features. The mechanics are the same; the syntax communicates intent more clearly.

**In this chapter:**
1. [Why split implicits up?](#1-why-split-implicits-up)
2. [`given` instances](#2-given-instances)
3. [`using` parameters](#3-using-parameters)
4. [`summon[T]`](#4-summont)
5. [`extension` methods](#5-extension-methods)
6. [Conversions: `Conversion[A, B]`](#6-conversions-conversiona-b)
7. [Importing givens](#7-importing-givens)
8. [Anonymous givens](#8-anonymous-givens)
9. [Migration from Scala 2 implicits](#9-migration-from-scala-2-implicits)

---

## 1. Why split implicits up?

In Scala 2, the single keyword `implicit` did four jobs:

- "this parameter is found from scope" (implicit parameters)
- "this is a value to find" (implicit val)
- "this is a method that auto-converts types" (implicit conversions)
- "this is a wrapper that adds methods" (implicit class)

Scala 3 split them by intent:

| Scala 2 | Scala 3 |
|---|---|
| `implicit val foo: T = ...` | `given foo: T = ...` |
| `def f(implicit x: T)` | `def f(using x: T)` |
| `implicit def aToB(a: A): B = ...` | `given Conversion[A, B] = (a: A) => ...` |
| `implicit class FooOps(x: Foo) { def bar = ... }` | `extension (x: Foo) def bar = ...` |
| `implicitly[T]` | `summon[T]` |

Same lookup rules. Same semantics. New, intent-revealing names.

---

## 2. `given` instances

Provide a value that the compiler can use:

```scala
// scala 3
given pi: Double = 3.14159
given Ordering[String] = Ordering.fromLessThan(_ < _)

trait Show[A]:
  def show(a: A): String

given Show[Int] with
  def show(n: Int): String = n.toString

given Show[String] with
  def show(s: String): String = s""""$s""""
```

`given X with` introduces an anonymous instance of trait X — like Scala 2's `new X { ... }` plus `implicit val`.

Givens can have parameters that are themselves looked up:

```scala
given listShow[A](using s: Show[A]): Show[List[A]] with
  def show(xs: List[A]): String =
    xs.map(s.show).mkString("[", ", ", "]")
```

---

## 3. `using` parameters

Replace `implicit` parameters:

```scala
def render[A](a: A)(using s: Show[A]): String = s.show(a)

render(42)                  // "42" — compiler finds Show[Int]
render(List("a", "b"))      // ["a", "b"]
```

The `using` clause must come *last* (like Scala 2's `implicit`). You can have multiple:

```scala
def f(x: Int)(using ec: ExecutionContext)(using log: Logger): Future[Int] = ???
```

Anonymous `using`:

```scala
def render[A](a: A)(using Show[A]): String =
  summon[Show[A]].show(a)
```

When you don't need the parameter name, the unnamed form is cleaner.

Context bound `[A: Show]` is unchanged in Scala 3 — and now it's clearly equivalent to `(using Show[A])`:

```scala
def render[A: Show](a: A): String = summon[Show[A]].show(a)
```

---

## 4. `summon[T]`

Summons a `given` of type T from the implicit scope. The Scala 3 replacement for `implicitly[T]`:

```scala
val ord = summon[Ordering[Int]]
ord.compare(1, 2)    // -1
```

Identical semantics to `implicitly`; the new name is simply more readable.

---

## 5. `extension` methods

Add methods to an existing type without inheritance — the cleaner replacement for `implicit class`:

```scala
extension (n: Int)
  def squared: Int = n * n
  def isOdd: Boolean = n % 2 != 0
  def cubed: Int = n * n * n

5.squared    // 25
3.isOdd      // true
2.cubed      // 8
```

Multiple extensions on the same type group naturally. No allocation — same `extends AnyVal` benefit you got in Scala 2 for free now.

You can use generic extensions:

```scala
extension [A](xs: List[A])
  def second: Option[A] = xs.drop(1).headOption
  def lastOpt: Option[A] = xs.lastOption
```

And extensions can use given parameters:

```scala
extension [A](a: A)(using Show[A])
  def display: String = summon[Show[A]].show(a)

42.display     // "42" — Show[Int] used implicitly
```

This combination — extension methods that take givens — is how Cats methods like `combine`, `show`, `map` (on type-class-backed values) get added in Scala 3.

---

## 6. Conversions: `Conversion[A, B]`

Implicit conversions are now explicit instances of `Conversion[A, B]`:

```scala
given Conversion[String, Int] with
  def apply(s: String): Int = s.toInt

val n: Int = "42"    // 42 — auto-converted
```

Or as a function:

```scala
given Conversion[String, Int] = _.toInt
```

You also have to import the language feature:

```scala
import scala.language.implicitConversions
```

This is intentionally more verbose than Scala 2's `implicit def` — you're meant to think harder about whether you really want a silent conversion. The answer is usually "no, use an extension method or an explicit constructor."

---

## 7. Importing givens

`given` instances aren't imported by `import path.*` — you must explicitly `import path.given`:

```scala
package mylib

object Instances:
  given Show[Int] with
    def show(n: Int): String = n.toString

// in another file:
import mylib.Instances.*       // does NOT bring in the given!
import mylib.Instances.given   // explicit; brings in givens
```

You can also be specific:

```scala
import mylib.Instances.{given Show[Int]}
```

This explicit import discipline avoids accidental implicit pollution — a very real Scala 2 problem.

---

## 8. Anonymous givens

If a given is only used by type, you don't need a name:

```scala
given Show[Boolean] with
  def show(b: Boolean): String = b.toString
```

The compiler synthesizes a fresh name. You can still summon by type — names matter only for explicit reference.

---

## 9. Migration from Scala 2 implicits

A rough mapping:

```scala
// scala 2:
implicit val ord: Ordering[Person] = Ordering.by(_.age)
implicit def listOrd[A](implicit ord: Ordering[A]): Ordering[List[A]] =
  Ordering.fromLessThan((a, b) => a.size < b.size)
implicit class IntOps(val n: Int) extends AnyVal {
  def squared: Int = n * n
}
implicit def stringToInt(s: String): Int = s.toInt
def show[A](a: A)(implicit s: Show[A]): String = s.show(a)

// scala 3:
given ord: Ordering[Person] = Ordering.by(_.age)
given listOrd[A](using ord: Ordering[A]): Ordering[List[A]] =
  Ordering.fromLessThan((a, b) => a.size < b.size)
extension (n: Int) def squared: Int = n * n
given Conversion[String, Int] = _.toInt
def show[A](a: A)(using s: Show[A]): String = s.show(a)
```

The Scala 2 syntax also still compiles in Scala 3 (with a deprecation warning) — you can migrate gradually.

---

## What you should now know

- The breakdown of Scala 2's overloaded `implicit` into `given`, `using`, `extension`, `Conversion`.
- How to define a `given` (named or anonymous) and use it through `summon`.
- `extension` methods as the modern replacement for `implicit class`.
- Why importing givens requires explicit `import path.given`.
- A direct mapping from Scala 2 idioms to Scala 3 equivalents.

The next chapter ([Chapter 12 — FP Foundations](12-functional-programming.md)) starts the FP-focused half of the guide.

---

[← Previous: Chapter 10 — Implicits (Scala 2)](10-implicits-scala2.md) | [Back to README](README.md) | [Next: Chapter 12 — FP Foundations →](12-functional-programming.md)
