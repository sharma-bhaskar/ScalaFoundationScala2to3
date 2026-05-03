# Chapter 5 — Pattern Matching

> Scala's `match` is a dramatic upgrade over Java's `switch`. It can deconstruct values, type-test, run guards, and (with sealed types) be checked for exhaustiveness. This chapter covers every pattern kind plus how to write your own.

**In this chapter:**
1. [The basic `match`](#1-the-basic-match)
2. [Wildcards and bindings](#2-wildcards-and-bindings)
3. [Literal patterns](#3-literal-patterns)
4. [Type patterns](#4-type-patterns)
5. [Constructor patterns (case classes, tuples)](#5-constructor-patterns-case-classes-tuples)
6. [Sequence patterns](#6-sequence-patterns)
7. [Guards](#7-guards)
8. [Variable binding with `@`](#8-variable-binding-with-)
9. [Sealed types and exhaustiveness](#9-sealed-types-and-exhaustiveness)
10. [Custom extractors (`unapply`)](#10-custom-extractors-unapply)
11. [Partial functions](#11-partial-functions)
12. [Regex patterns](#12-regex-patterns)
13. [Pattern matching in `val` and `for`](#13-pattern-matching-in-val-and-for)

---

## 1. The basic `match`

```scala
def describe(n: Int): String = n match {
  case 0 => "zero"
  case 1 => "one"
  case 2 => "two"
  case _ => "many"
}

describe(0)    // "zero"
describe(7)    // "many"
```

`match` is an expression — it evaluates to whichever branch matches. The compiler picks the first matching `case` from top to bottom. `_` is the catchall.

> **Scala 3 note:** Scala 3 supports `match` without curly braces using indentation:
> ```scala
> // scala 3
> def describe(n: Int): String = n match
>   case 0 => "zero"
>   case _ => "many"
> ```

---

## 2. Wildcards and bindings

The `_` matches anything and discards. To match anything *and* bind it to a name, use a lowercase identifier:

```scala
def describe(x: Any): String = x match {
  case 0    => "zero"
  case n: Int => s"int $n"      // binds matched value to n
  case _    => "something else"
}
```

Note Scala's case rule: lowercase identifiers in pattern position **bind** new variables; uppercase ones (or backticks) refer to existing ones:

```scala
val pi = 3.14
def f(x: Double): String = x match {
  case `pi` => "pi"          // matches existing pi value (3.14)
  case pi   => s"got $pi"    // BINDS a new local pi — matches anything!
}
```

Always use backticks to refer to an outer constant, or a capitalized name. This is the #1 pattern-matching gotcha for new Scala programmers.

---

## 3. Literal patterns

Match against a specific value:

```scala
def kind(c: Char): String = c match {
  case 'a' | 'e' | 'i' | 'o' | 'u' => "vowel"
  case _ if c.isLetter             => "consonant"
  case _                            => "other"
}
```

`|` is alternation — the case matches if any of the alternatives matches.

---

## 4. Type patterns

Test the runtime type and bind:

```scala
def describe(x: Any): String = x match {
  case s: String       => s"string of length ${s.length}"
  case n: Int          => s"int $n"
  case xs: List[_]     => s"list of size ${xs.size}"
  case _               => "unknown"
}
```

A few gotchas:

- **Type erasure.** `case xs: List[Int]` doesn't actually check the element type — at runtime, all `List` is just `List`. Use `List[_]` to make the unchecked nature explicit.
- **Generic types** can be matched with type parameters using `match` + an additional check (or use `ClassTag`).
- **Type tests are not free** — `isInstanceOf` checks happen at runtime. Use sealed traits + ADT pattern matching when possible (next sections).

---

## 5. Constructor patterns (case classes, tuples)

This is where matching shines:

```scala
case class Point(x: Int, y: Int)

def origin(p: Point): String = p match {
  case Point(0, 0)         => "origin"
  case Point(0, y)         => s"y-axis at $y"
  case Point(x, 0)         => s"x-axis at $x"
  case Point(x, y)         => s"($x, $y)"
}
```

The compiler turns each `Point(...)` pattern into a call to `Point.unapply` (auto-synthesized for `case class`) that extracts the fields.

Tuples deconstruct directly:

```scala
def swap(pair: (Int, Int)): (Int, Int) = pair match {
  case (a, b) => (b, a)
}
```

Nested patterns work:

```scala
case class Address(city: String, zip: String)
case class Person(name: String, addr: Address)

def cityOf(p: Person): String = p match {
  case Person(_, Address(city, _)) => city
}
```

---

## 6. Sequence patterns

Match the structure of a `Seq`:

```scala
def describe(xs: List[Int]): String = xs match {
  case Nil               => "empty"
  case List(x)           => s"single: $x"
  case List(x, y)        => s"pair: $x, $y"
  case head :: tail      => s"head=$head, tail size ${tail.size}"
  case _                 => "anything"
}
```

Use `_*` for "the rest, however many":

```scala
def describe(xs: List[Int]): String = xs match {
  case List(1, 2, _*)            => "starts with 1, 2"
  case List(_, _, _, rest @ _*)  => s"3+ elements; rest = $rest"
  case _                         => "other"
}
```

`rest @ _*` binds the rest to `rest`. Without the binding, you can use `_*` as a sink.

The `head :: tail` pattern is the canonical way to recurse over a List:

```scala
def length[A](xs: List[A]): Int = xs match {
  case Nil       => 0
  case _ :: rest => 1 + length(rest)
}
```

---

## 7. Guards

A boolean condition after the pattern, prefixed with `if`:

```scala
def classify(n: Int): String = n match {
  case n if n < 0     => "negative"
  case 0              => "zero"
  case n if n < 10    => "small positive"
  case n if n < 100   => "medium"
  case _              => "large"
}
```

Guards run *after* the pattern matches but before the body. The compiler doesn't include guards in exhaustiveness analysis.

---

## 8. Variable binding with `@`

Bind the matched value (or a sub-pattern) to a name with `@`:

```scala
case class User(name: String, age: Int)

def greet(u: User): String = u match {
  case adult @ User(_, age) if age >= 18 =>
    s"Hello, ${adult.name}!"
  case _ =>
    "Hello, kid!"
}
```

Here, `adult` binds the *whole* `User`, while `age` binds the field. Useful when you want to test something deep in a structure but also keep the outer object.

Nested binding works too:

```scala
val data = List(1, 2, 3, 4, 5)

data match {
  case head :: tail @ List(_, _, _, _) =>
    println(s"head=$head, tail of length 4: $tail")
  case _ => println("doesn't fit")
}
```

---

## 9. Sealed types and exhaustiveness

A `sealed` trait or class can only be extended in the same file. The compiler can therefore check that your `match` covers every case:

```scala
sealed trait Shape
case class Circle(r: Double) extends Shape
case class Square(side: Double) extends Shape
case class Rectangle(w: Double, h: Double) extends Shape

def area(s: Shape): Double = s match {
  case Circle(r)       => math.Pi * r * r
  case Square(side)    => side * side
  // forgot Rectangle — compiler warns!
}
```

> warning: match may not be exhaustive. It would fail on the following input: Rectangle(_, _)

Sealed traits + case classes form an **algebraic data type (ADT)** — a closed set of variants the compiler can reason about. This is one of Scala's strongest patterns. We come back to ADTs in [Chapter 12](12-functional-programming.md).

> **Scala 3 note:** Scala 3 has `enum` for ADTs, which is shorter and clearer:
> ```scala
> // scala 3
> enum Shape:
>   case Circle(r: Double)
>   case Square(side: Double)
>   case Rectangle(w: Double, h: Double)
> ```

---

## 10. Custom extractors (`unapply`)

Any object with an `unapply` method can be used as a pattern. Case classes get one for free. You can write your own:

```scala
object Email {
  def unapply(s: String): Option[(String, String)] = {
    val parts = s.split("@")
    if (parts.length == 2) Some((parts(0), parts(1))) else None
  }
}

"bhaskar@example.com" match {
  case Email(local, domain) => println(s"$local at $domain")
  case _                    => println("not an email")
}
// bhaskar at example.com
```

**Signature rules:**

- `unapply(input): Boolean` — for boolean tests with no extracted values.
- `unapply(input): Option[A]` — single extraction.
- `unapply(input): Option[(A, B, ...)]` — multiple extractions.
- `unapplySeq(input): Option[Seq[A]]` — variable-length, for `case Email(parts @ _*)`.

Example of `unapplySeq`:

```scala
object IntList {
  def unapplySeq(s: String): Option[Seq[Int]] =
    scala.util.Try(s.split(",").map(_.trim.toInt).toSeq).toOption
}

"1, 2, 3" match {
  case IntList(a, b, c)    => println(s"three: $a, $b, $c")
  case IntList(xs @ _*)    => println(s"sequence of ${xs.size}")
  case _                    => println("not a list")
}
```

---

## 11. Partial functions

A `PartialFunction[A, B]` is a function defined only on *some* inputs. Built from `case` clauses without a wrapping `match`:

```scala
val onlyEvens: PartialFunction[Int, String] = {
  case n if n % 2 == 0 => s"$n is even"
}

onlyEvens.isDefinedAt(2)    // true
onlyEvens.isDefinedAt(3)    // false

onlyEvens(2)                // "2 is even"
onlyEvens(3)                // throws MatchError
```

The collection method `collect` takes a `PartialFunction` and uses it to map *and* filter:

```scala
List(1, 2, 3, 4, 5).collect {
  case n if n % 2 == 0 => n * 10
}
// List(20, 40)
```

`collect` keeps the elements for which the function is defined, applying it to each.

`PartialFunction` is also what Akka's `receive` is built on (each case is a message handler).

---

## 12. Regex patterns

A `Regex` value can be used in pattern matching, with capture groups becoming bindings:

```scala
val date = """(\d{4})-(\d{2})-(\d{2})""".r

"2026-05-03" match {
  case date(year, month, day) => s"$day/$month/$year"
  case _                       => "not a date"
}
// "03/05/2026"
```

`.r` on a string creates a `Regex`. The pattern's capture groups become positional bindings in the case.

Useful for parsing one-liners. For anything complex, prefer a real parser (parser combinators, fastparse, etc.).

---

## 13. Pattern matching in `val` and `for`

Pattern matching isn't only for `match` — you can use it on the left-hand side of any binding.

```scala
val (a, b) = (1, 2)              // destructure tuple
val Point(x, y) = Point(1, 2)    // destructure case class
val head :: tail = List(1, 2, 3) // destructure list

for {
  (k, v) <- Map("a" -> 1, "b" -> 2)   // pattern in generator
} println(s"$k = $v")

for {
  Person(name, age) <- people         // only People; others would skip
} println(name)
```

**Caveat:** if the pattern is *refutable* (it might not match every value), Scala 2 issues a warning; Scala 3 requires the `case` keyword:

```scala
// Scala 2: warning
val head :: _ = someList   // works, but warns; throws on empty list

// Scala 3:
val case head :: _ = someList   // explicit

// Either:
someList match {
  case head :: _ => ...
  case _         => ...
}
```

In a `for`, the matching naturally filters — non-matching elements are just skipped:

```scala
val mixed: List[Any] = List(1, "a", 2, "b", 3)
val ints = for {
  case i: Int <- mixed     // Scala 3 syntax
} yield i * 10
// List(10, 20, 30)
```

---

## What you should now know

- Every pattern kind: wildcard, literal, type, constructor, sequence, regex.
- How `|` (alternation), `if` (guards), and `@` (binding) modify patterns.
- The lowercase-vs-Capitalized binding rule and why backticks matter.
- Sealed traits + case classes for compiler-checked exhaustive matches.
- How to write your own extractor with `unapply` and `unapplySeq`.
- Partial functions and where they show up (`collect`, Akka).
- Patterns in `val` definitions and `for` comprehensions.

The next chapter ([Chapter 6 — Classes and Objects](06-classes-and-objects.md)) shifts to the OOP side of Scala.

---

[← Previous: Chapter 4 — Collections](04-collections.md) | [Back to README](README.md) | [Next: Chapter 6 — Classes and Objects →](06-classes-and-objects.md)
