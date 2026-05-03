# Chapter 10 — Implicits (Scala 2)

> Implicits are Scala 2's mechanism for letting the compiler fill in arguments, conversions, and methods automatically. They're powerful, central to type-class programming, and sometimes infuriating to debug. This chapter covers every form.

**In this chapter:**
1. [What "implicit" means](#1-what-implicit-means)
2. [Implicit parameters](#2-implicit-parameters)
3. [Implicit `val`/`def`](#3-implicit-valdef)
4. [`implicitly[T]`](#4-implicitlyt)
5. [Implicit resolution rules](#5-implicit-resolution-rules)
6. [Implicit conversions](#6-implicit-conversions)
7. [`implicit class` (extension methods)](#7-implicit-class-extension-methods)
8. [Evidence parameters: `=:=`, `<:<`](#8-evidence-parameters--)
9. [Type class encoding](#9-type-class-encoding)
10. [Debugging implicits](#10-debugging-implicits)

---

## 1. What "implicit" means

The keyword `implicit` marks something the compiler can fill in automatically. Three kinds of things can be implicit:

- **Implicit parameters** — declared with `implicit`, found by the compiler at the call site.
- **Implicit conversions** — methods that convert one type to another.
- **Implicit classes** — wrap a type to add methods (extension methods).

All three search the same *implicit scope*: imports, the current scope, and the companion objects of involved types.

> **Scala 3 note:** Scala 3 splits all of this into **givens**, **using clauses**, **extension methods**, and explicit `Conversion[A, B]` instances. See [Chapter 11](11-given-using-scala3.md) for the modern spelling. The mechanics are the same; the syntax changed.

---

## 2. Implicit parameters

A method can declare a parameter list as `implicit`. Callers don't need to pass it; the compiler finds a value of the right type from the implicit scope.

```scala
def greet(implicit name: String): String = s"hello $name"

implicit val defaultName: String = "Bhaskar"

greet                  // "hello Bhaskar"  — name supplied implicitly
greet("Alice")         // "hello Alice"     — explicit override
```

Real-world usage: the `ExecutionContext` for `Future`s, configuration, type class instances:

```scala
import scala.concurrent.{Future, ExecutionContext}

def fetch(url: String)(implicit ec: ExecutionContext): Future[String] = ???
```

The `(implicit ec: ExecutionContext)` is the canonical example — every `Future`-returning method threads the EC through implicitly so callers don't have to pass it explicitly.

You can have at most **one** implicit parameter list per method, and it must come last:

```scala
def f(x: Int)(y: Int)(implicit z: Int): Int = x + y + z   // OK
// def f(x: Int)(implicit y: Int)(z: Int): Int             // not OK
```

---

## 3. Implicit `val`/`def`

`implicit val` provides a value:

```scala
implicit val pi: Double = 3.14159
implicit val emoji: String = ""

def show(implicit prefix: String): String = s"$prefix scala"

show    // " scala"
```

`implicit def` provides a method whose return value is used:

```scala
implicit def stringToInt(s: String): Int = s.toInt    // implicit conversion
implicit def listToOpt[A](xs: List[A]): Option[A] = xs.headOption
```

`implicit def` with parameters can build an implicit *based on other implicits*:

```scala
implicit def listOrdering[A](implicit ord: Ordering[A]): Ordering[List[A]] =
  Ordering.by(_.size)   // (just an example; not a great Ordering)
```

This is how Cats and the standard library compose type class instances.

`implicit object` works the same way for cases where an entire object should serve as an implicit value:

```scala
implicit object IntShow extends Show[Int] {
  def show(n: Int): String = n.toString
}
```

---

## 4. `implicitly[T]`

`implicitly[T]` summons the implicit value of type T from the current scope:

```scala
implicit val n: Int = 42
val x = implicitly[Int]    // 42
```

Useful in generic methods:

```scala
def compare[A](a: A, b: A)(implicit ord: Ordering[A]): Int = {
  // ord is in scope as a regular variable here.
  ord.compare(a, b)
}

def compare2[A: Ordering](a: A, b: A): Int = {
  // context bound: ord is implicit but unnamed.
  // Use implicitly[Ordering[A]] to access it:
  implicitly[Ordering[A]].compare(a, b)
}
```

---

## 5. Implicit resolution rules

When the compiler needs an implicit value of type T, it searches:

1. **Local scope** — any `implicit val`/`def`/`object` visible at the call site (including imports).
2. **Companion object of T** — implicits defined in `object T { ... }`.
3. **Companion objects of T's type parameters and supertypes** — recursively, for parameterized types like `List[Foo]` (looks in `List`'s and `Foo`'s companions).

If exactly one match is found, the compiler uses it. If multiple match, it picks the **most specific** (subtype-wise); if there's no clear winner, it errors with "ambiguous implicit values."

A very common pattern: define type class instances in the companion of the type:

```scala
trait Show[A] { def show(a: A): String }

case class Person(name: String, age: Int)

object Person {
  implicit val personShow: Show[Person] = (p: Person) => s"${p.name} (${p.age})"
}

def display[A](a: A)(implicit s: Show[A]): String = s.show(a)

display(Person("Bhaskar", 30))    // works without imports
```

By placing `personShow` in `Person`'s companion, callers can use `display(p)` anywhere — no imports required.

---

## 6. Implicit conversions

An `implicit def` from A to B lets the compiler convert silently:

```scala
implicit def intToLong(n: Int): Long = n.toLong

val l: Long = 42    // 42 (Int) auto-converts to 42L (Long)
```

The standard library uses these to make Java types interop with Scala (e.g., `String` -> `StringOps`, `Array[Int]` -> `ArrayOps[Int]`).

**Implicit conversions are dangerous.** They change semantics silently and complicate reading code. Default position: **don't write them**. Modern Scala uses extension methods (next section) for almost everything implicit conversions used to do.

In Scala 2.13+, you must explicitly enable language feature `implicitConversions`:

```scala
import scala.language.implicitConversions
```

> **Scala 3 note:** Scala 3 uses `given Conversion[A, B]` instead of `implicit def` for conversions. See [Chapter 11](11-given-using-scala3.md).

---

## 7. `implicit class` (extension methods)

The cleaner cousin of implicit conversions. Add methods to an existing type:

```scala
implicit class IntOps(val n: Int) extends AnyVal {
  def squared: Int = n * n
  def isOdd: Boolean = n % 2 != 0
}

5.squared    // 25
3.isOdd      // true
```

How it works: `5.squared` becomes `new IntOps(5).squared`. The `extends AnyVal` makes it a value class — no allocation in the common case (the `IntOps` is "erased"; the call becomes a static method call on `Int`).

Three rules for `implicit class`:

1. Must be in a `class`, `object`, `package object`, or `trait` (not at top level in Scala 2 — fixed in Scala 3).
2. Constructor must take exactly one parameter.
3. The class can't have a companion object (Scala 2 limitation).

> **Scala 3 note:** Scala 3 prefers `extension` methods:
> ```scala
> // scala 3
> extension (n: Int)
>   def squared: Int = n * n
>   def isOdd: Boolean = n % 2 != 0
> ```

---

## 8. Evidence parameters: `=:=`, `<:<`

Some implicits encode *facts about types* — "A is the same as B," "A is a subtype of B":

```scala
def onlyForLists[A, B](xs: List[A])(implicit ev: A =:= B): List[B] = {
  // The implicit ev: A =:= B is summoned by the compiler ONLY IF A and B are the same type.
  // Once we have it, we can use ev to coerce.
  xs.map(ev(_))
}

onlyForLists[Int, Int](List(1, 2, 3))    // works — 0 == B
// onlyForLists[Int, String](List(1, 2)) // doesn't compile — Int =/= String
```

Useful for restricting generic methods to certain type relationships. Standard evidence types:

- `A =:= B` — A and B are the same type.
- `A <:< B` — A is a subtype of B.
- `A =!= B` — A is not B (less standard; in some libs).

Real example from the stdlib: `Map#toList` doesn't make sense for arbitrary `Iterable`, only when it's `(K, V)`. Evidence parameters make that constraint expressible.

---

## 9. Type class encoding

Implicits + parameterized traits = type classes. The pattern:

```scala
// 1. The type class trait.
trait Show[A] {
  def show(a: A): String
}

// 2. Companion provides instances.
object Show {
  def apply[A](implicit s: Show[A]): Show[A] = s   // summoner

  implicit val intShow: Show[Int] = (n: Int) => n.toString
  implicit val strShow: Show[String] = (s: String) => s""""$s""""

  implicit def listShow[A](implicit s: Show[A]): Show[List[A]] = (xs: List[A]) =>
    xs.map(s.show).mkString("[", ", ", "]")
}

// 3. Use it:
def render[A: Show](a: A): String = Show[A].show(a)

render(42)                    // "42"
render("hello")               // ""hello""
render(List(1, 2, 3))         // "[1, 2, 3]"
```

This encoding gives you:
- **Ad-hoc polymorphism** — different behavior for different types, chosen at compile time.
- **Open extensibility** — you can add a `Show` instance for any type, even types you don't own.
- **No inheritance required** — the type doesn't have to extend anything.

`Show`, `Eq`, `Ord`, `Functor`, `Monad` — all type classes. We dedicate [Chapter 16](16-typeclasses.md) to the pattern itself.

---

## 10. Debugging implicits

When the compiler can't find an implicit, the error is sometimes inscrutable:

```
could not find implicit value for parameter ec: scala.concurrent.ExecutionContext
```

Tips:

1. **Read the type the compiler is looking for.** Often it's a type class instance you forgot to import.

2. **Enable `-Xlog-implicits`** in your `build.sbt`:
   ```scala
   scalacOptions += "-Xlog-implicits"
   ```
   This dumps every implicit search the compiler does. Verbose, but it shows you what was tried and rejected.

3. **`reify` and `:type` in REPL** — useful for one-off checks.

4. **`shapeless` / `simulacrum`** — these libraries help with type class boilerplate.

5. **Common gotcha**: forgot to import the implicits.
   ```scala
   import scala.concurrent.ExecutionContext.Implicits.global
   ```

6. **Check companion objects.** If you defined an implicit in a companion, callers don't need to import it. If you defined it in a regular object, they do.

---

## What you should now know

- Implicit parameters, values, and methods.
- The implicit scope rules and how to predict resolution.
- Implicit conversions — what they are, why they're dangerous, and how to enable them.
- `implicit class` for extension methods.
- Evidence parameters (`=:=`, `<:<`) for type-relationship constraints.
- The type class encoding using implicit parameters and instances.
- How to debug implicit-resolution failures.

The next chapter ([Chapter 11 — Given/Using Scala 3](11-given-using-scala3.md)) shows the modern Scala 3 spelling for everything in this chapter.

---

[← Previous: Chapter 9 — Advanced Types](09-advanced-types.md) | [Back to README](README.md) | [Next: Chapter 11 — Given/Using →](11-given-using-scala3.md)
