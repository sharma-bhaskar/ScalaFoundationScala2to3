# Chapter 1 — Language Basics

> The smallest pieces of Scala: how to bind values, what the type hierarchy looks like, what an expression is, and why operators are just methods. Read this once and you have the vocabulary every later chapter assumes.

**In this chapter:**
1. [`val` and `var`](#1-val-and-var)
2. [Type inference](#2-type-inference)
3. [The type hierarchy](#3-the-type-hierarchy)
4. [Literals](#4-literals)
5. [Expressions vs statements](#5-expressions-vs-statements)
6. [`if` / `else`](#6-if--else)
7. [`while` and `do`/`while`](#7-while-and-dowhile)
8. [Basic `for` loops](#8-basic-for-loops)
9. [Blocks and scope](#9-blocks-and-scope)
10. [Operators are method calls](#10-operators-are-method-calls)
11. [The `Unit` type](#11-the-unit-type)
12. [Comments and ScalaDoc](#12-comments-and-scaladoc)

---

## 1. `val` and `var`

A binding gives a name to a value. Scala has two kinds:

```scala
val pi = 3.14159     // immutable: cannot be reassigned
var counter = 0      // mutable: can be reassigned

counter = counter + 1   // OK
// pi = 3.0           // compile error: reassignment to val
```

`val` is the default. Reach for `var` only when you actually need mutation; idiomatic Scala leans on `val` and creates new values rather than modifying old ones.

```scala
val name: String = "Bhaskar"   // type annotation explicit
val age = 30                   // type inferred as Int
```

The type annotation is optional when the compiler can infer it (almost always for literals). Annotating *public* APIs is still a good habit — it documents intent and prevents inference from drifting if the implementation changes.

> **Scala 3 note:** Same syntax. Scala 3 also adds `inline val` for compile-time constants used in metaprogramming.

### `lazy val`

A `lazy val` is initialized on first use, not at declaration:

```scala
lazy val expensive = {
  println("computing...")
  (1 to 1_000_000).sum
}

println("before access")
println(expensive)
println(expensive)

// Output:
// before access
// computing...
// 500000500000
// 500000500000
```

Notice "computing..." prints once. Lazy vals are thread-safe by default — Scala uses double-checked locking under the hood. We cover them in depth in [Chapter 14](14-lazy-evaluation.md).

---

## 2. Type inference

Scala infers types from the right-hand side:

```scala
val n = 42                    // Int
val x = 3.14                  // Double
val name = "Bhaskar"          // String
val flag = true               // Boolean
val list = List(1, 2, 3)      // List[Int]
val tuple = (1, "a", true)    // (Int, String, Boolean)
```

Inference also works for return types of methods:

```scala
def double(n: Int) = n * 2          // return type inferred: Int
def double(n: Int): Int = n * 2     // explicit; same thing
```

**Style guide:** infer locals freely, annotate public method/value signatures explicitly. The signature is part of the contract.

```scala
// good
def parseAge(input: String): Option[Int] = input.toIntOption

// works but the contract is now hidden
def parseAge(input: String) = input.toIntOption
```

### What Scala can't infer

- Recursive method return types — must be annotated.
- Sometimes the "most general" type isn't what you want; annotate to widen.

```scala
// won't compile — can't infer return type for recursive method
// def fact(n: Int) = if (n == 0) 1 else n * fact(n - 1)

def fact(n: Int): Int = if (n == 0) 1 else n * fact(n - 1)   // OK
```

```scala
val animals = List(new Dog, new Cat)           // inferred List[Animal] only if Dog/Cat share that supertype
val animals: List[Animal] = List(new Dog, new Cat)  // explicit when you want widening
```

---

## 3. The type hierarchy

Every type in Scala descends from `Any`. There is no "primitive vs object" split as there is in Java — at the language level, *everything* is an object.

```
                        Any
                       /   \
                  AnyVal    AnyRef  (= java.lang.Object)
                  /  | \      |  \
              Int Double Boolean  String, List, your classes...
                  ...
                       \   /
                       Null  (subtype of every AnyRef)
                         |
                     Nothing  (subtype of every type)
```

**`Any`** is the root. Has `==`, `!=`, `equals`, `hashCode`, `toString`, `##`.

**`AnyVal`** is the parent of *value types* — `Int`, `Long`, `Double`, `Float`, `Char`, `Byte`, `Short`, `Boolean`, `Unit`. They map directly to JVM primitives when possible (no boxing) but appear as objects to your code.

**`AnyRef`** is everything else — equivalent to `java.lang.Object`. All your classes, the standard collections, `String`, etc.

**`Null`** is the type of `null`. It is a subtype of every `AnyRef`, which is what makes `val s: String = null` compile (idiomatic Scala avoids it; see [Chapter 13](13-error-handling.md) on `Option`).

**`Nothing`** is a subtype of *every* type. It has no values. Useful as the result type of expressions that don't return — like exceptions:

```scala
def fail(msg: String): Nothing = throw new RuntimeException(msg)

val x: Int = if (cond) 42 else fail("nope")   // works because Nothing <: Int
```

`Nothing` is also the inferred element type of an empty list (`List[Nothing]` is a `List[A]` for any `A` thanks to covariance — see [Chapter 8](08-type-system.md)).

```scala
val empty = List()   // List[Nothing]
val withInts: List[Int] = empty   // OK
```

> **Scala 3 note:** Scala 3 introduces *explicit nulls* (opt-in via `-Yexplicit-nulls`), where `Null` is no longer a subtype of `AnyRef`. This makes `val s: String = null` a compile error and matches modern best practice (Kotlin/Swift-style).

---

## 4. Literals

### Numeric literals

```scala
val i: Int    = 42
val l: Long   = 42L         // L suffix
val f: Float  = 3.14f       // f suffix
val d: Double = 3.14        // default for decimals
val hex       = 0xFF        // 255
val bin       = 0b1010      // 10  (Scala 2.13+)
val readable  = 1_000_000   // underscores for legibility (Scala 2.13+)
```

### Boolean

```scala
val t = true
val f = false
```

### Character

```scala
val c: Char = 'A'           // single quotes
val newline = '\n'
val unicode = 'é'      // 'é'
```

### String

```scala
val s = "hello"
val multi = """line one
                |line two
                |line three""".stripMargin
```

The triple-quoted form preserves newlines and ignores escape sequences. `stripMargin` removes whitespace up to and including the `|` on each line.

String interpolation gets its own chapter ([Chapter 3](03-strings-and-interpolation.md)). The minimal taste:

```scala
val name = "Bhaskar"
val greeting = s"hello, $name"        // "hello, Bhaskar"
val math = s"1 + 1 = ${1 + 1}"        // "1 + 1 = 2"
val padded = f"$pi%1.2f"              // "3.14"  (printf-style)
```

### Symbol literals (deprecated)

```scala
val sym = 'foo     // Symbol("foo")  — DEPRECATED in 2.13, removed in 3
```

You'll see `Symbol` in older code. In modern Scala, just use a `String` or define an `enum`.

### Tuples

```scala
val pair: (Int, String)        = (1, "one")
val triple: (Int, String, Bool)= (1, "one", true)

pair._1   // 1
pair._2   // "one"

val (a, b) = pair    // destructure: a = 1, b = "one"
```

A tuple is shorthand for `Tuple2`, `Tuple3`, …, up to `Tuple22` in Scala 2. Scala 3 lifts the 22 limit with native variadic tuples.

> **Scala 3 note:** Tuples in Scala 3 can be of any length, and there are operations like `*:` (cons), `++` (concat), `Zip`, etc. as type-level operations.

---

## 5. Expressions vs statements

In Scala, **almost everything is an expression** — it produces a value. This is one of the most important shifts from Java.

```scala
val x = if (cond) 1 else 2          // if/else is an expression
val y = { val a = 1; val b = 2; a + b }   // a block evaluates to its last expression
```

Compare to Java, where `if` is a *statement* and you'd have to use the ternary `cond ? 1 : 2` for the same effect.

The few things that are statements (don't yield a useful value): `var x = ...` (assignment), top-level imports, package declarations. They produce `Unit`.

This expression-orientation is why Scala code chains so cleanly:

```scala
val result =
  if (input.isEmpty) "empty"
  else if (input.length < 3) "short"
  else input.toUpperCase
```

---

## 6. `if` / `else`

Used as both a control flow and a value-producing expression:

```scala
// as expression
val parity = if (n % 2 == 0) "even" else "odd"

// as control flow
if (debug) println(s"value is $x")
```

**No ternary operator.** You don't need one — `if/else` is already an expression.

If you omit `else`, the result type is `Unit` (because the missing branch implicitly returns `()`):

```scala
val x = if (cond) 42      // x: AnyVal — usually not what you want!
val y: Option[Int] = if (cond) Some(42) else None   // explicit; better
```

> **Scala 3 note:** Scala 3 supports a parens-free, indentation-based form:
> ```scala
> // scala 3
> val parity = if n % 2 == 0 then "even" else "odd"
> ```
> The Scala 2 form (with parens) still compiles in Scala 3.

---

## 7. `while` and `do`/`while`

Scala has them but they're rarely used in idiomatic code (we prefer recursion or higher-order functions):

```scala
var i = 0
while (i < 5) {
  println(i)
  i += 1
}

var j = 0
do {
  println(j)
  j += 1
} while (j < 5)
```

Both are statements (return `Unit`). They require mutation (`var`), which is a code smell in functional style — most loops are better expressed as `(0 until 5).foreach(println)` or a recursive function.

> **Scala 3 note:** `do/while` was removed in Scala 3. Use `while (true) { ...; if (!cond) return }` or recursion instead.

---

## 8. Basic `for` loops

Scala's `for` is much more than a counter loop. Here we cover the simple case; full coverage in [Chapter 15](15-for-comprehensions.md).

### Iterate

```scala
for (i <- 1 to 5) println(i)        // 1 2 3 4 5  (inclusive)
for (i <- 1 until 5) println(i)     // 1 2 3 4    (exclusive)

for (s <- List("a", "b", "c")) println(s)
```

### Multiple generators

```scala
for {
  i <- 1 to 3
  j <- 1 to 3
} println(s"($i,$j)")

// (1,1) (1,2) (1,3) (2,1) (2,2) (2,3) (3,1) (3,2) (3,3)
```

### Filter (guards)

```scala
for {
  i <- 1 to 10
  if i % 2 == 0
} println(i)
// 2 4 6 8 10
```

### `yield` to produce a collection

```scala
val squares = for (i <- 1 to 5) yield i * i
// Vector(1, 4, 9, 16, 25)
```

`for/yield` is the gateway to monadic programming in Scala — every monad (Option, Either, Future, IO) becomes ergonomic in a `for` because it desugars to `flatMap` + `map`. Worth previewing:

```scala
val maybeNum: Option[Int] = Some(2)
val maybeDbl: Option[Int] = Some(3)

val sum: Option[Int] =
  for {
    a <- maybeNum
    b <- maybeDbl
  } yield a + b
// Some(5)
```

We come back to this idea in [Chapter 15](15-for-comprehensions.md) and [Chapter 17](17-monads-and-fp-abstractions.md).

---

## 9. Blocks and scope

A block `{ ... }` is itself an expression that evaluates to its last expression:

```scala
val area = {
  val pi = 3.14
  val r  = 2
  pi * r * r
}
// area: Double = 12.56
```

`pi` and `r` exist only inside the block. Bindings in an inner block can shadow outer ones:

```scala
val x = 1
val y = {
  val x = 99   // shadows the outer x within this block
  x + 1
}
// x = 1, y = 100
```

A method body, an `if` branch, a `for` body — all are blocks under the hood.

---

## 10. Operators are method calls

There's no special "operator" syntax in Scala — what looks like an operator is just a method whose name happens to be punctuation:

```scala
1 + 2          // syntactic sugar for...
1.+(2)         // ...this. Both compile to the same thing.

"abc" * 3      // "abcabcabc"  — String has a `*` method
"abc".*(3)     // same
```

This means you can define your own operators just by giving methods symbol-y names:

```scala
case class Vec(x: Double, y: Double) {
  def +(other: Vec): Vec = Vec(x + other.x, y + other.y)
  def *(scalar: Double): Vec = Vec(x * scalar, y * scalar)
}

val v1 = Vec(1, 2)
val v2 = Vec(3, 4)
v1 + v2         // Vec(4.0, 6.0)
v1 * 2.0        // Vec(2.0, 4.0)
```

### The `unary_` prefix

Prefix operators (`-x`, `!flag`, `~bits`, `+x`) are encoded as `unary_` methods:

```scala
case class Vec(x: Double, y: Double) {
  def unary_- : Vec = Vec(-x, -y)
}

val v = Vec(3, 4)
-v   // Vec(-3.0, -4.0)
```

Only `unary_+`, `unary_-`, `unary_!`, `unary_~` are allowed.

### Right-associative methods

Methods ending with `:` are right-associative — `a :: list` actually means `list.::(a)`:

```scala
val xs = 1 :: 2 :: 3 :: Nil    // List(1, 2, 3)
// equivalent to: Nil.::(3).::(2).::(1)
```

This is why `::` (cons) reads naturally: prepend an element to a list.

### Precedence

Determined by the *first character* of the method name. Roughly:

```
(highest)  * / %
           + -
           : (right-assoc)
           = !
           < >
           &
           ^
           |
(lowest)   alphabetic identifiers
```

So `1 + 2 * 3` is `1 + (2 * 3)` as you'd expect. When in doubt, parenthesize.

---

## 11. The `Unit` type

`Unit` is Scala's "nothing useful here" type — analogous to `void` in Java/C, but it's an actual type with a single value, written `()`:

```scala
val nothingMuch: Unit = ()     // the only value of type Unit

def log(msg: String): Unit = println(msg)
```

Methods that exist for side effects (printing, mutating state, sending an HTTP response) return `Unit`.

A useful idiom: when you want a method to always return something a caller can assemble in expressions:

```scala
def saveToDb(record: Record): Either[DbError, Unit] = ...
```

Returning `Either[DbError, Unit]` lets the caller chain `flatMap`s without caring about a meaningless success value, while still carrying the error possibility.

---

## 12. Comments and ScalaDoc

```scala
// single-line

/* multi-line
   comment */

/** ScalaDoc — generated as HTML by the `doc` task.
  *
  * @param x the input integer
  * @return  twice the input
  */
def double(x: Int): Int = x * 2
```

`sbt doc` generates an HTML site from your ScalaDoc — useful for libraries.

Common ScalaDoc tags:

- `@param name description`
- `@return description`
- `@throws ExceptionType description`
- `@see [[OtherClass]]` or `@see [[https://example.com]]`
- `@since 1.2.0`
- `@deprecated message`
- `@example { val x = 1 }`

---

## What you should now know

- The difference between `val`, `var`, and `lazy val`
- That every type sits in the `Any` / `AnyVal` / `AnyRef` / `Nothing` / `Null` lattice, and what each one means
- Numeric, string, character, boolean, and tuple literals
- That `if`, `for/yield`, blocks, and most other constructs are *expressions*, not statements
- That operators are methods — including how to define your own
- That `Unit` is Scala's `void`-equivalent type with a single value `()`

The next chapter ([Chapter 2 — Functions and Methods](02-functions-and-methods.md)) builds on these basics with everything Scala offers around `def`, function values, currying, higher-order functions, by-name parameters, and eta-expansion.

---

[← Back to README](README.md) | [Next: Chapter 2 — Functions and Methods →](02-functions-and-methods.md)
