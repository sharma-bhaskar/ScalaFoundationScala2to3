# Chapter 13 — Error Handling

> Scala has exceptions like Java does, but idiomatic Scala uses *typed error containers* — `Option`, `Either`, `Try`, `Validated` — to make failure visible in signatures.

**In this chapter:**
1. [`Option[A]`](#1-optiona)
2. [`Either[L, R]`](#2-eitherl-r)
3. [`Try[A]`](#3-trya)
4. [`Validated[E, A]`](#4-validatede-a)
5. [When exceptions are still appropriate](#5-when-exceptions-are-still-appropriate)
6. [Conversions between containers](#6-conversions-between-containers)

---

## 1. `Option[A]`

The "no null" type. An `Option[A]` is either `Some(a)` or `None`:

```scala
def parseInt(s: String): Option[Int] = s.toIntOption

parseInt("42")      // Some(42)
parseInt("abc")     // None
```

The whole point: a function returning `Option[A]` is **honest about failure** in its type. The compiler forces you to handle the absence case.

### Working with Options

```scala
val maybe: Option[Int] = Some(5)

maybe.isDefined            // true
maybe.isEmpty              // false
maybe.getOrElse(0)         // 5     (or 0 if None)
maybe.orElse(Some(99))     // Some(5) (or Some(99) if None)
maybe.foreach(println)     // prints 5; nothing if None

// transformations
maybe.map(_ * 2)           // Some(10)
maybe.flatMap(n => Some(n + 1))   // Some(6)
maybe.filter(_ > 0)        // Some(5)
maybe.fold(0)(_ * 2)       // 10  (default 0 if None, else map and return)

// pattern matching
maybe match {
  case Some(n) => println(s"got $n")
  case None    => println("nothing")
}
```

### Composing Options

Multiple `Option`s with `for`:

```scala
def divide(a: Int, b: Int): Option[Int] = if (b == 0) None else Some(a / b)

val result = for {
  x <- divide(10, 2)     // Some(5)
  y <- divide(x, 0)      // None  — the whole thing short-circuits
} yield y
// None
```

If any step is `None`, the whole `for` is `None`. This is the monadic short-circuit pattern, covered in depth in [Chapter 15](15-for-comprehensions.md) and [Chapter 17](17-monads-and-fp-abstractions.md).

### Common pitfalls

- **`option.get`** — throws `NoSuchElementException` if `None`. Almost never the right choice. Use `getOrElse`, `fold`, or pattern matching instead.
- **`Option(null)` returns `None`**, but `Some(null)` returns `Some(null)` (a real Some containing null!). Use `Option(x)` when wrapping anything that *might* be null; never `Some(x)` for a possibly-null x.

```scala
val s: String = null
Some(s)           // Some(null)  — bad!
Option(s)         // None        — correct
```

---

## 2. `Either[L, R]`

When you need to know *why* something failed, not just *that* it did. `Either[L, R]` is `Left(l)` or `Right(r)`. By convention, `Right` is success and `Left` is failure (mnemonic: "right" is "correct").

```scala
def parseInt(s: String): Either[String, Int] =
  s.toIntOption.toRight(s"not an int: $s")

parseInt("42")    // Right(42)
parseInt("abc")   // Left("not an int: abc")
```

`Either` is **right-biased** in Scala 2.12+ — `map`/`flatMap`/`for` work on the `Right`:

```scala
val result = for {
  a <- parseInt("10")
  b <- parseInt("3")
  c <- parseInt("xyz")
} yield a + b + c
// Left("not an int: xyz")
```

The first `Left` short-circuits the rest, just like with Option.

### Useful methods

```scala
val r: Either[String, Int] = Right(42)

r.map(_ * 2)              // Right(84)
r.flatMap(n => Right(n))  // Right(42)
r.getOrElse(0)            // 42
r.fold(_.length, _ + 1)   // 43  — collapse: Left case + Right case
r.swap                    // Left(42) — Right and Left swap roles
r.toOption                // Some(42); Left becomes None
r.left.map(_ + "!")       // recover into a different Left
```

### Error types

For real applications, model errors as a sealed trait:

```scala
sealed trait AppError
case class NotFound(id: String)  extends AppError
case class Invalid(reason: String) extends AppError
case class DbError(cause: Throwable) extends AppError

def loadUser(id: String): Either[AppError, User] = ???
```

Now the type tells you exactly which errors are possible. The match is exhaustive and the caller has to handle each.

---

## 3. `Try[A]`

A wrapper specifically for code that might throw. `Try[A]` is `Success(a)` or `Failure(throwable)`:

```scala
import scala.util.{Try, Success, Failure}

val result: Try[Int] = Try("42".toInt)
val bad: Try[Int]    = Try("xyz".toInt)

result match {
  case Success(n) => println(n)
  case Failure(e) => println(s"failed: $e")
}
```

`Try` is right-biased on `Success`:

```scala
Try("42".toInt).map(_ * 2)        // Success(84)
Try("xyz".toInt).map(_ * 2)       // Failure(NumberFormatException...)

Try("42".toInt).recover { case _: NumberFormatException => 0 }   // Success(42)
Try("xyz".toInt).recover { case _: NumberFormatException => 0 }  // Success(0)
```

When to use `Try` vs `Either`:

- **`Try`** when you're wrapping code that throws (Java APIs, parsers, etc.) — the error is a `Throwable`.
- **`Either[E, A]`** when you control the error type and want a specific ADT — preferred in pure FP code.

You can convert: `Try.toEither` gives `Either[Throwable, A]`.

---

## 4. `Validated[E, A]`

From the **Cats** library. Like `Either`, but designed to **accumulate** errors instead of short-circuiting.

```scala
import cats.data.Validated
import cats.implicits._

def validateName(s: String): Validated[List[String], String] =
  if (s.nonEmpty) s.valid else List("name cannot be empty").invalid

def validateAge(n: Int): Validated[List[String], Int] =
  if (n >= 0 && n < 150) n.valid else List("age out of range").invalid

case class User(name: String, age: Int)

(validateName(""), validateAge(-1))
  .mapN(User.apply)
// Invalid(List("name cannot be empty", "age out of range"))   — both errors!
```

Compare to `Either`:

```scala
for {
  n <- validateName("")    // returns Left immediately
  a <- validateAge(-1)     // never runs
} yield User(n, a)
// Left("name cannot be empty")  — only the first error
```

Use `Validated` when you want to report **all** the things wrong (form validation, batch processing). Use `Either` when failures should short-circuit (don't try to use the DB if the auth check failed).

---

## 5. When exceptions are still appropriate

Don't be dogmatic. Exceptions are right for:

- **Truly exceptional, unrecoverable situations**: out of memory, hardware fault, programmer bugs (assertion failures).
- **Internal invariants** where the caller couldn't reasonably recover: `require(n > 0)`, `assert(...)`.
- **Java interop** boundaries — you'll throw and catch JVM exceptions when bridging.

For everyday domain failures (validation, missing entries, parse errors, IO errors that callers might handle), prefer typed containers.

A common mid-level idiom: **catch exceptions at the boundary**, convert to typed errors:

```scala
def loadConfig(): Either[ConfigError, Config] =
  Try(realIOLoadConfig())
    .toEither
    .left.map {
      case _: java.io.FileNotFoundException => ConfigError.Missing
      case other                            => ConfigError.Unknown(other)
    }
```

The infrastructure layer can throw; the domain layer sees a proper `Either`.

---

## 6. Conversions between containers

```scala
// Option → Either
Some(1).toRight("no value")       // Right(1)
None.toRight("no value")          // Left("no value")
Some(1).toLeft("no error")        // Left(1)

// Either → Option
Right(1).toOption                  // Some(1)
Left("err").toOption               // None

// Try → Either
Success(1).toEither                // Right(1)
Failure(new Exception).toEither    // Left(java.lang.Exception)

// Try → Option
Success(1).toOption                // Some(1)

// Either → Try (less common)
Right(1).toTry                     // Success(1)
Left(new Exception).toTry          // Failure(...)
```

Knowing the conversions makes interop easy. A common pattern:

```scala
def parseAge(s: String): Either[String, Int] =
  s.toIntOption                            // Option[Int]
    .toRight(s"invalid number: $s")        // Either[String, Int]
    .filterOrElse(_ >= 0, "must be non-negative")
```

---

## What you should now know

- The four typed error containers — `Option`, `Either`, `Try`, `Validated` — and when each fits.
- That `Option`, `Either`, and `Try` short-circuit; `Validated` accumulates.
- Why typed errors are better than throwing for ordinary domain failures.
- Where exceptions still belong, and how to catch-and-convert at the boundary.
- The conversions between containers.

The next chapter ([Chapter 14 — Lazy Evaluation](14-lazy-evaluation.md)) is shorter — we cover `lazy val`, by-name parameters, and `LazyList`.

---

[← Previous: Chapter 12 — FP Foundations](12-functional-programming.md) | [Back to README](README.md) | [Next: Chapter 14 — Lazy Evaluation →](14-lazy-evaluation.md)
