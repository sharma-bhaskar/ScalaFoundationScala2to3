# Chapter 25 — Best Practices and Idioms

> Opinionated guidance for writing Scala that future-you (and your team) will thank you for.

**In this chapter:**
1. [Idiomatic shape](#1-idiomatic-shape)
2. [Naming](#2-naming)
3. [When to use what](#3-when-to-use-what)
4. [Things to avoid](#4-things-to-avoid)
5. [Effects: pick one and stick with it](#5-effects-pick-one-and-stick-with-it)
6. [Scalafmt + scalafix as guard rails](#6-scalafmt--scalafix-as-guard-rails)

---

## 1. Idiomatic shape

A Scala module that reads well usually has these properties:

- **`val` over `var`.** Reach for `var` only when you actually need mutation (rare).
- **Immutable data.** `case class` with `copy` is the default modeling tool.
- **ADTs over flags.** `sealed trait` + variants beats `Boolean` flags + nullable fields.
- **Functions return values; they don't throw for ordinary failures.** Use `Either`, `Option`, or your effect type.
- **Small functions, composed.** Two-line functions are not a code smell — they're a signal of healthy decomposition.
- **Explicit return types on public APIs.** Inferred return types are fine inside; signatures are part of the contract.
- **Tail recursion or `foldLeft` over `var` + `while`** for loops.

A small example that touches several of these:

```scala
sealed trait LoginResult
case class Success(token: String) extends LoginResult
case object Locked                 extends LoginResult
case object BadCredentials         extends LoginResult

def login(creds: Credentials, attempts: Int): LoginResult = {
  if (attempts >= 3) Locked
  else if (verify(creds)) Success(generateToken(creds))
  else BadCredentials
}
```

Notice: closed set of outcomes, immutable inputs, no exceptions, explicit return type, three lines.

---

## 2. Naming

Standard Scala naming:

- **Types**: `PascalCase`. `User`, `OrderService`, `HttpClient`.
- **Methods, vals, vars**: `camelCase`. `findUser`, `maxRetries`.
- **Constants**: `camelCase` (or `UPPER_CASE` if it really is a constant in the C sense). `defaultPort`, `MaxAttempts`.
- **Packages**: lowercase. `com.acme.app.user`.
- **Type parameters**: single uppercase. `A`, `B`, `K`, `V`, `F[_]`. Use full names (e.g., `Item`) only when there are many parameters and clarity demands it.

Avoid:

- Hungarian notation (`strName`, `iCount`).
- Abbreviations beyond conventional ones (`ctx` for `context` is fine; `unr` for `userNameRequest` is not).
- Long names that just restate the type. A `request: Request` parameter doesn't need a longer name.

For boolean-returning methods, prefer `isX`, `hasX`, `canX`. For predicates, use names that read naturally:

```scala
def isExpired: Boolean = ...
def hasPermission(role: Role): Boolean = ...
def shouldRetry: Boolean = ...
```

---

## 3. When to use what

**`case class` vs `class`**: case class for data; class for things that have *behavior* without value-equality semantics (a service, a connection pool, a state machine where instances are distinct).

**`sealed trait` vs `enum` (Scala 3)**: enum for closed sets where you don't need a class hierarchy. Sealed trait for ADTs with rich case classes.

**`Option` vs `Either`**: Option when "missing" is the only meaningful failure. Either when callers benefit from knowing *why* it failed.

**`Future` vs `IO`**: Future for quick code, especially when the rest of the codebase already uses Future. IO when you want referential transparency, cancellation, resource safety, fibers.

**`def` vs `val` (function value)**: `def` by default. `val` only when you need to pass the function around or store it.

**Recursion vs `foldLeft`**: `foldLeft` first. Tail-recursive helper if the fold doesn't fit. Recursion without `@tailrec` only for tree-shaped problems.

**`for/yield` vs explicit `flatMap`/`map`**: for-comprehension when you have 3+ steps or want named bindings. Explicit chain when it's one or two steps and reads more directly.

**Implicit/given imports**: keep narrow — `import cats.syntax.option._` over `import cats.implicits._` when you can. Your IDE can help with the auto-import.

---

## 4. Things to avoid

### `null` and exceptions in your domain

`null` is for the Java boundary; never produce it from Scala code. Use `Option`. Same for exceptions in business logic — use `Either` or your effect type's error channel.

### `option.get`, `either.right.get`

Throws on failure. Use `getOrElse`, pattern matching, or `fold`.

### `Await.result` outside boundaries

It blocks. Future composition exists for a reason.

### `var` in shared state

A `var` in a class is a code smell unless the class is explicitly mutable (`Counter`, `ListBuffer`). For shared/mutable state across threads, use atomic refs or pass it through an effect.

### `asInstanceOf`

Almost always wrong. If you reach for it, your modeling is off — usually you wanted a sealed trait or a generic type parameter.

### `_*` mismatched with actual seq

Common gotcha:

```scala
def totalLength(strs: String*): Int = strs.map(_.length).sum

val list = List("a", "bb", "ccc")
totalLength(list)        // wrong! takes a single Seq[String], not varargs
totalLength(list: _*)    // right — splat
```

### Trying to be too clever with implicits

Layered implicit conversions make code unreadable. Prefer extension methods and explicit imports.

### `Future`'s eager start mixed with by-name semantics

Don't accept a `Future` as a by-name parameter — the caller has already started it; the by-name doesn't help. If you want lazy async, take a thunk: `() => Future[A]` or `IO[A]`.

---

## 5. Effects: pick one and stick with it

If you're using `IO` (Cats Effect) for half your codebase and `Future` for the other half, you're doing twice the integration work. Lockdown:

- **Cats Effect** for new FP-style codebases. `IO` everywhere; `unsafeRunSync` only at the entry point.
- **ZIO** if your team prefers ZIO's environment-typed approach.
- **Akka with Future** for legacy or actor-heavy codebases.
- **Plain Future** if you're integrating with a Future-heavy ecosystem (e.g., older Akka HTTP).

Stick to one within a service. Across services is fine — that's a network boundary.

---

## 6. Scalafmt + scalafix as guard rails

**Scalafmt** is the formatter. Configure once, run on every commit. Add `.scalafmt.conf`:

```hocon
version = "3.8.3"
runner.dialect = scala213

maxColumn = 100
align.preset = most

rewrite.rules = [
  RedundantBraces
  RedundantParens
  SortImports
]
```

Run: `sbt scalafmtAll` (with sbt-scalafmt plugin) or `scalafmt` (CLI). Most teams run it as a pre-commit hook.

**Scalafix** is for code rewrites and lint rules. Migrations between Scala versions (`-Xsource:3` source compatibility, removed deprecations) are largely automated by Scalafix rules. Setup: `.scalafix.conf`:

```hocon
rules = [
  DisableSyntax
  RemoveUnused
  ProcedureSyntax
]
```

Run: `sbt scalafixAll`. Configure to fail builds on lint violations once your team agrees on rules.

These two tools eliminate huge categories of style debate. Set them up early and never look back.

---

## What you should now know

- The shape of idiomatic Scala — immutable data, ADTs, typed errors, small functions.
- Naming conventions.
- A decision matrix for the common "which feature?" questions.
- The list of common anti-patterns to avoid.
- Why "pick one effect type" matters and what to pick.
- Scalafmt + scalafix as the team-discipline tools.

The next (and final) chapter ([Chapter 26 — Cheatsheet](26-cheatsheet.md)) is a one-page reference for everything in this guide.

---

[← Previous: Chapter 24 — Java Interop](24-java-interop.md) | [Back to README](README.md) | [Next: Chapter 26 — Cheatsheet →](26-cheatsheet.md)
