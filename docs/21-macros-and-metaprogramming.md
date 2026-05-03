# Chapter 21 — Macros and Metaprogramming

> Macros let you generate code at compile time. Scala 2's def macros are infamous for being hard. Scala 3 redesigned them to be much more approachable. This chapter covers both, and the simpler `inline` mechanism that handles many former-macro use cases.

**In this chapter:**
1. [`inline` (Scala 3)](#1-inline-scala-3)
2. [Scala 2 def macros (briefly)](#2-scala-2-def-macros-briefly)
3. [Scala 3 quoted macros](#3-scala-3-quoted-macros)
4. [Reflection and `ClassTag`](#4-reflection-and-classtag)
5. [Practical example: a JSON codec](#5-practical-example-a-json-codec)

---

## 1. `inline` (Scala 3)

The simplest form of compile-time programming. `inline` tells the compiler to inline the function body at the call site:

```scala
// scala 3
inline def square(x: Int): Int = x * x

square(5)    // compiler replaces this with `5 * 5`
```

For purely-substitution cases, that's it. But `inline` gets more powerful with `inline match` and `inline if`:

```scala
// scala 3
inline def constant[T](inline x: T): T = x

inline def isString[T](x: T): Boolean = inline x match
  case _: String => true
  case _         => false

isString("hi")    // true (decided at compile time)
isString(42)      // false (decided at compile time)
```

Many things that needed full macros in Scala 2 — derivation helpers, debug helpers, validation — can be done with `inline` alone in Scala 3.

---

## 2. Scala 2 def macros (briefly)

Scala 2 macros are powerful but invasive. You write a macro implementation against the compiler's AST API and reference it from a `def`:

```scala
// scala 2
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Macros {
  def debug[A](a: A): Unit = macro debugImpl[A]

  def debugImpl[A: c.WeakTypeTag](c: blackbox.Context)(a: c.Expr[A]): c.Expr[Unit] = {
    import c.universe._
    val src = show(a.tree)
    c.Expr[Unit](q"""println($src + " = " + $a)""")
  }
}

// usage:
debug(1 + 2)
// at compile time, the macro expands to:
// println("1.+(2) = " + (1 + 2))
```

Why this is painful:

- The macro lives in a *separate module/jar* from its callers.
- Errors are shown in terms of generated code.
- The AST API is hostile and version-coupled.
- IDE support is hit-or-miss.

For this reason, Scala 2 macros are mostly used in libraries (Shapeless, Scalatest's `assert`, Quill, Slick) — application code rarely writes them.

---

## 3. Scala 3 quoted macros

Scala 3's macro system is more approachable: quote/splice syntax, separate phases, type-safe AST manipulation.

```scala
// scala 3
import scala.quoted._

inline def debug[A](inline a: A): Unit = ${ debugImpl('a) }

def debugImpl[A: Type](a: Expr[A])(using Quotes): Expr[Unit] = {
  import quotes.reflect._
  val src = a.show
  '{ println(${Expr(src)} + " = " + $a) }
}

// usage:
debug(1 + 2)
// expands to: println("1.+(2) = " + (1 + 2))
```

`'{ ... }` is a **quote** — code as data. `${ ... }` is a **splice** — inject an expression. The two compose: the macro returns an `Expr[Unit]` that the compiler stitches into the call site.

Compared to Scala 2:

- Errors are clearer (in terms of the actual user code).
- The API is type-safe (Expr[A] tracks the type).
- Same module/file as callers (no separate build).
- IDE support is much better.

Macros are still niche — they're for library authors. But Scala 3 makes them tractable.

---

## 4. Reflection and `ClassTag`

Runtime reflection lives in `scala.reflect`. Most uses you'll see are for `ClassTag` / `TypeTag`:

```scala
import scala.reflect.ClassTag

def newArray[A: ClassTag](size: Int): Array[A] =
  new Array[A](size)

newArray[Int](10)         // works
newArray[String](10)
```

`ClassTag[A]` carries enough type info at runtime to materialize `Array[A]`. Without it, the JVM erases the type and `Array[A]` is impossible.

For deeper reflection (member listing, dynamic invocation), Scala 2 has `scala.reflect.runtime.universe.*`. **Avoid it.** Reflection is slow, brittle, and IDE-unfriendly. Scala 3 has scaled the runtime API back significantly; use compile-time tools (inline, macros) instead.

For Java-interop reflection (`Class<?>`, `Method`), use the Java APIs directly — faster, well-known.

---

## 5. Practical example: a JSON codec

The motivating use of macros: derive serialization automatically. Libraries like **Circe**, **upickle**, **jsoniter-scala** generate codecs for case classes at compile time, with no runtime reflection.

Circe (Scala 2 + 3):

```scala
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

case class Person(name: String, age: Int)

implicit val decoder: Decoder[Person] = deriveDecoder
implicit val encoder: Encoder[Person] = deriveEncoder

Person("Bhaskar", 30).asJson.spaces2
// {
//   "name": "Bhaskar",
//   "age": 30
// }
```

`deriveDecoder` is a macro (Scala 2) / inline + match (Scala 3). It walks the case class's fields at compile time, generating an Encoder/Decoder. Zero runtime reflection. Errors at compile time if a field type doesn't have an implicit codec.

This is **the** real-world reason to know macros exist. Even if you never write one, you'll use ones libraries provide for codecs, schema generation, query DSLs, validations, etc.

---

## What you should now know

- `inline` (Scala 3) for simple compile-time substitution and `match`/`if`.
- Scala 2 def macros — what they are, why they're painful.
- Scala 3 quoted macros — quote `'{ }` / splice `${ }` as the modern syntax.
- `ClassTag` for runtime type evidence.
- That deep runtime reflection is best avoided.
- Codec libraries (Circe, jsoniter) as the primary place application code meets macros.

The next chapter ([Chapter 22 — Build with sbt](22-build-tools-sbt.md)) covers the build tool that ties everything together.

---

[← Previous: Chapter 20 — Actors and Streams](20-actors-and-streams.md) | [Back to README](README.md) | [Next: Chapter 22 — sbt →](22-build-tools-sbt.md)
