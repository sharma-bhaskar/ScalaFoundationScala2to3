# Chapter 2 — Functions and Methods

> Scala has both *methods* (`def`) and *function values* (`val f = (x: Int) => x + 1`). They look similar, but they're not the same thing. This chapter explains the distinction, every way to define them, and the FP-flavored features (HOFs, currying, by-name) that make Scala expressive.

**In this chapter:**
1. [`def`: methods](#1-def-methods)
2. [Function values vs methods](#2-function-values-vs-methods)
3. [Anonymous functions](#3-anonymous-functions)
4. [Higher-order functions](#4-higher-order-functions)
5. [The `_` placeholder](#5-the-_-placeholder)
6. [Default and named arguments](#6-default-and-named-arguments)
7. [Currying and multiple parameter lists](#7-currying-and-multiple-parameter-lists)
8. [Partial application](#8-partial-application)
9. [Eta-expansion](#9-eta-expansion)
10. [By-name parameters](#10-by-name-parameters)
11. [Polymorphic methods](#11-polymorphic-methods)
12. [`Nothing`, `Any`, `Unit` as return types](#12-nothing-any-unit-as-return-types)

---

## 1. `def`: methods

A method belongs to a class, object, trait, or block. Define one with `def`:

```scala
def add(a: Int, b: Int): Int = a + b

add(2, 3)    // 5
```

- `add` is the name.
- `(a: Int, b: Int)` is the parameter list.
- `: Int` is the return type (optional if the body's type is inferable).
- `= a + b` is the body — a single expression.

Multi-line bodies use a block:

```scala
def hypotenuse(a: Double, b: Double): Double = {
  val a2 = a * a
  val b2 = b * b
  math.sqrt(a2 + b2)
}
```

> **Scala 3 note:** Curly braces are optional with significant indentation:
> ```scala
> // scala 3
> def hypotenuse(a: Double, b: Double): Double =
>   val a2 = a * a
>   val b2 = b * b
>   math.sqrt(a2 + b2)
> ```

A method with no parameters can be defined with or without parentheses:

```scala
def greet(): String = "hello"   // empty parens
def name: String = "Bhaskar"     // no parens

greet()   // call with parens
name      // call without
```

Convention: parens on side-effecting methods (`stop()`, `close()`), no parens on pure accessors (`size`, `length`, `name`). It's a hint to the reader, nothing more.

---

## 2. Function values vs methods

A **function value** is an *object* of a `FunctionN` type. Define one as a `val`:

```scala
val addFn: (Int, Int) => Int = (a, b) => a + b
addFn(2, 3)   // 5
```

`(Int, Int) => Int` is sugar for `Function2[Int, Int, Int]`. Likewise:

- `() => A` is `Function0[A]`
- `A => B` is `Function1[A, B]`
- `(A, B) => C` is `Function2[A, B, C]`
- … up to `Function22` in Scala 2.

The differences from a method:

| | `def` (method) | `val` (function value) |
|---|---|---|
| What it is | A method on the enclosing class/object | An object implementing `FunctionN` |
| Can be passed to higher-order functions? | Yes, via *eta-expansion* | Yes, directly |
| Can have type parameters? | Yes (`def f[T](x: T) = ...`) | No (function values aren't generic) |
| Can have default args, by-name, currying nicely? | Yes | Limited |
| Memory cost | Zero (just a method) | One object allocation |
| Can be `lazy val` / depend on other class state | Yes (it's a class member) | Yes (so can a `val`) |

**Rule of thumb:** define methods with `def`. Use function values only when you need to pass them around or assign them to variables.

```scala
def increment(n: Int): Int = n + 1                  // method — usual choice
val incrementFn: Int => Int = n => n + 1            // function value — when needed
```

---

## 3. Anonymous functions

A function value with no name. Most often passed to a higher-order function:

```scala
List(1, 2, 3).map(x => x * 2)   // List(2, 4, 6)
```

`(x => x * 2)` is an anonymous function — equivalent to `(x: Int) => x * 2`. The compiler infers `x: Int` from the context (we're calling `.map` on `List[Int]`).

The general syntax:

```scala
(p1: T1, p2: T2, ...) => expression
```

Examples:

```scala
() => 42                                    // takes nothing, returns 42
(x: Int) => x + 1                           // increment
(a: Int, b: Int) => a + b                   // sum
(s: String) => { val t = s.trim; t.length }  // multi-statement body in a block
```

If the parameter types can be inferred, you can omit them:

```scala
val ints = List(1, 2, 3)
ints.filter(x => x > 1)        // x inferred as Int
ints.foldLeft(0)((acc, x) => acc + x)   // both inferred
```

---

## 4. Higher-order functions

A function that takes a function as a parameter, or returns one. The collection library is built on these.

```scala
def applyTwice(f: Int => Int, x: Int): Int = f(f(x))

applyTwice(_ + 1, 5)    // 7
applyTwice(_ * 2, 5)    // 20
```

The same idea, with collection methods:

```scala
List(1, 2, 3, 4, 5).filter(_ % 2 == 0)        // List(2, 4)
List(1, 2, 3, 4, 5).map(_ * 10)                // List(10, 20, 30, 40, 50)
List(1, 2, 3, 4, 5).foldLeft(0)(_ + _)         // 15
List("a", "b", "c").mkString(", ")             // "a, b, c"
```

A function that *returns* a function:

```scala
def adder(n: Int): Int => Int = x => x + n

val plus5 = adder(5)
plus5(10)    // 15
```

`adder(5)` returns a new function — a *closure* — that has captured `n = 5`. This is one of the cornerstones of FP: building specialized functions by partial configuration.

---

## 5. The `_` placeholder

Scala has a remarkable shorthand for anonymous functions: `_` stands for "the next argument."

```scala
List(1, 2, 3).map(_ * 2)             // same as: x => x * 2
List(1, 2, 3).filter(_ > 1)          // same as: x => x > 1
List(1, 2, 3).foldLeft(0)(_ + _)     // same as: (acc, x) => acc + x
```

Each `_` is a separate parameter, in left-to-right order. So `_ + _` is `(a, b) => a + b`.

The `_` can also turn a method into a function value:

```scala
def square(x: Int): Int = x * x

val sqFn = square _                  // turns the method into a Function1
sqFn(3)                              // 9
```

Or you can apply it partially:

```scala
def add(a: Int, b: Int): Int = a + b

val addFive = add(5, _: Int)         // partial application: a Function1
addFive(10)                          // 15
```

The `_` is one of those Scala features that's powerful and confusing in equal measure. Use it for short, obvious cases. Spell out the parameter when the lambda gets longer than ~20 characters.

---

## 6. Default and named arguments

Methods can have default values:

```scala
def connect(host: String, port: Int = 8080, ssl: Boolean = false): Unit = {
  println(s"connecting to $host:$port (ssl=$ssl)")
}

connect("api.example.com")                         // port 8080, ssl false
connect("api.example.com", 443, true)              // explicit
connect("api.example.com", ssl = true)             // named — skip middle
connect(host = "api.example.com", port = 443)      // all named
```

Named arguments make calls self-documenting and let you reorder arguments. They're especially nice with `case class.copy(...)`:

```scala
case class Order(id: String, qty: Int, total: BigDecimal)
val o1 = Order("o-1", 2, 30.0)
val o2 = o1.copy(qty = 5)             // bump qty, keep id and total
```

> **Style note:** Use defaults for *truly optional* parameters. If a parameter has a sensible default but the caller almost always overrides it, it's a smell.

---

## 7. Currying and multiple parameter lists

A method can have multiple parameter lists. This is "curried" form:

```scala
def addCurried(a: Int)(b: Int): Int = a + b

addCurried(2)(3)         // 5
```

Each call applies one list. You can apply just the first list and get a function back:

```scala
val add2 = addCurried(2) _      // type: Int => Int
add2(10)                        // 12
```

Why bother? Three real reasons:

**1. Type inference flows left to right across parameter lists.** Critical when you want a higher-order parameter to infer its type from earlier arguments:

```scala
// flat — doesn't infer well
def fold[A, B](xs: List[A], z: B, f: (B, A) => B): B = ???

xs.fold(0, (acc, x) => acc + x)    // Scala may need explicit types

// curried — infers cleanly
def fold[A, B](xs: List[A])(z: B)(f: (B, A) => B): B = ???

xs.fold(0)((acc, x) => acc + x)    // f's types inferred from xs and z
```

This is why you see methods like `foldLeft(z)(f)` in the standard library.

**2. Block-syntax DSLs.**

```scala
def using[R <: AutoCloseable, A](resource: R)(action: R => A): A = {
  try action(resource) finally resource.close()
}

using(new java.io.FileWriter("out.txt")) { w =>
  w.write("hello")
}
```

The `{ w => ... }` block reads like a built-in language construct. This trick is everywhere in Scala libraries.

**3. Partial application.** As shown above, applying just the first list gives you a reusable specialized function.

---

## 8. Partial application

Apply some arguments, get a function expecting the rest:

```scala
def log(level: String, msg: String): Unit = println(s"[$level] $msg")

val info  = log("INFO", _: String)
val warn  = log("WARN", _: String)
val error = log("ERROR", _: String)

info("server started")    // [INFO] server started
warn("low disk")          // [WARN] low disk
error("crashed")          // [ERROR] crashed
```

For curried methods, just apply one list:

```scala
def add(a: Int)(b: Int): Int = a + b

val plusFive = add(5) _    // Int => Int

plusFive(10)               // 15
```

In Scala 3 the trailing `_` is no longer required.

---

## 9. Eta-expansion

When you pass a method where a function is expected, Scala automatically converts the method to a function value. That conversion is **eta-expansion**.

```scala
def double(x: Int): Int = x * 2

List(1, 2, 3).map(double)     // works! map expects Int => Int

// behind the scenes, this is equivalent to:
List(1, 2, 3).map(x => double(x))
```

In Scala 2 you sometimes need the explicit `_`:

```scala
val doubleFn: Int => Int = double _    // Scala 2: required
val doubleFn: Int => Int = double      // Scala 3: not required
```

Scala 3 made eta-expansion automatic in nearly every position; Scala 2 requires the `_` when the expected type is a `Function` but the compiler can't figure it out from context.

---

## 10. By-name parameters

A *by-name* parameter is not evaluated at the call site — it's evaluated each time you reference it inside the method body. The syntax: `=> A`.

```scala
def myAssert(cond: Boolean, message: => String): Unit = {
  if (!cond) throw new AssertionError(message)
}

myAssert(true, expensiveErrorMessage())    // expensiveErrorMessage NEVER runs
myAssert(false, expensiveErrorMessage())   // runs once
```

A by-value parameter would have evaluated `expensiveErrorMessage()` *before* `myAssert` was called, even when the assertion passed. By-name lets the caller hand over an *expression*, evaluated lazily.

This is what makes Scala libraries able to invent things that look like control flow:

```scala
def repeat(n: Int)(block: => Unit): Unit = {
  var i = 0
  while (i < n) { block; i += 1 }
}

repeat(3) { println("hi") }
// hi
// hi
// hi
```

`block` is a by-name parameter; each iteration evaluates it again.

**Performance note:** a by-name parameter is compiled to a `Function0[A]`, so it allocates a small object on each call. Don't worry about it unless you're in a tight loop.

For a value you want to evaluate at most once but lazily, combine by-name with `lazy val`:

```scala
def maybe[A](cond: Boolean, value: => A): Option[A] = {
  lazy val v = value
  if (cond) Some(v) else None
}
```

---

## 11. Polymorphic methods

Methods can take type parameters:

```scala
def identity[A](a: A): A = a

identity(42)        // Int — A = Int
identity("hello")   // String — A = String
identity(List(1))   // List[Int]
```

Multiple type parameters and constraints work as you'd expect:

```scala
def pair[A, B](a: A, b: B): (A, B) = (a, b)

def first[A, B](pair: (A, B)): A = pair._1
def second[A, B](pair: (A, B)): B = pair._2
```

Type parameters can have *bounds*:

```scala
def biggest[A <: Comparable[A]](xs: List[A]): A =
  xs.reduce((a, b) => if (a.compareTo(b) > 0) a else b)
```

`A <: Comparable[A]` says "A must be a subtype of `Comparable[A]`." Bounds are covered in depth in [Chapter 8](08-type-system.md).

---

## 12. `Nothing`, `Any`, `Unit` as return types

A few special return types worth knowing:

**`Unit`** — the method returns nothing useful (i.e., it's run for its side effects):

```scala
def log(msg: String): Unit = println(msg)
```

**`Nothing`** — the method *never returns normally*. Either it throws or it loops forever. Useful in helper methods that always fail:

```scala
def fail(msg: String): Nothing = throw new RuntimeException(msg)

val x: Int = if (cond) 42 else fail("bad")   // OK because Nothing <: Int
```

`Nothing` is a subtype of everything, so the type checker accepts `fail("bad")` in any position.

**`Any`** — the method might return anything. Generally a code smell. If you find yourself returning `Any`, you probably want a sealed trait or a generic type parameter instead.

```scala
def parse(s: String): Any = ???         // bad
sealed trait Parsed                      // better
case class IntVal(n: Int) extends Parsed
case class StrVal(s: String) extends Parsed
def parse(s: String): Parsed = ???
```

---

## What you should now know

- The difference between methods (`def`) and function values (`val f: A => B = ...`).
- How to write anonymous functions and use the `_` placeholder.
- That higher-order functions and closures are first-class in Scala.
- Default and named arguments, including `copy(field = ...)` on case classes.
- Currying, why multiple parameter lists exist (type inference, DSLs, partial application).
- Eta-expansion (the bridge between methods and function values).
- By-name parameters (`=> A`) and how they enable lazy evaluation and DSLs.
- Polymorphic methods with type parameters.
- The `Unit`, `Nothing`, and `Any` return types.

The next chapter ([Chapter 3 — Strings](03-strings-and-interpolation.md)) covers string literals, interpolators, and how to roll your own.

---

[← Previous: Chapter 1 — Language Basics](01-language-basics.md) | [Back to README](README.md) | [Next: Chapter 3 — Strings →](03-strings-and-interpolation.md)
