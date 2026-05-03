# Scala, Comprehensive

> A practical, example-driven reference covering Scala from first `val` to advanced functional and effect-system programming.
> Scala **2** is the primary syntax (because most production code is still on it). Scala **3** differences are noted inline at every concept that changed.

This is a learning resource and a reference. Read it top to bottom for a complete tour, or jump to the chapter you need. Every concept has a runnable example.

---

## How to use this guide

- **Each chapter is a single file.** Open one, read top to bottom; every section builds on the previous.
- **All examples compile.** Most are self-contained snippets you can paste into the REPL (`scala`) or the Scala-CLI (`scala-cli run snippet.scala`).
- **Scala 3 notes appear inline** in callouts wherever syntax or semantics differ.
- **Cross-references** between chapters use the file name, e.g. `[implicits](10-implicits-scala2.md)`.

A working setup: install JDK 17+, install [Coursier](https://get-coursier.io/) (`cs setup`), then `sbt new scala/hello-world.g8` for a starter project, or `scala-cli` for one-file experiments.

---

## Table of contents

### Part I — Foundations

- **[00 — Setup and Tooling](00-setup-and-tooling.md)**
  - Installing the JDK, Scala, sbt, and Scala-CLI
  - The REPL and worksheets
  - IDE choices: IntelliJ vs Metals (VS Code)
  - Hello World, three ways
  - The shape of a real project (`build.sbt`, `project/`, `src/main/scala`, `src/test/scala`)

- **[01 — Language Basics](01-language-basics.md)**
  - `val` and `var`, type inference, the `Unit` type
  - Primitive types and the `AnyVal` / `AnyRef` hierarchy
  - Literals: integers, floats, characters, strings, symbols, tuples
  - Expressions vs statements (everything is an expression)
  - `if` / `else`, `while`, `do`/`while`, `for` (basics)
  - Blocks and scope
  - Operators are method calls (`1 + 2` is `1.+(2)`)
  - Comments and ScalaDoc

- **[02 — Functions and Methods](02-functions-and-methods.md)**
  - `def` (methods) vs `val` (function values)
  - Parameter lists, default arguments, named arguments
  - Currying and multiple parameter lists
  - Higher-order functions
  - Anonymous functions and the `_` placeholder
  - Eta-expansion (turning a method into a function)
  - By-name parameters (`=> A`)
  - Polymorphic methods (type parameters)
  - `Nothing`, `Any`, `Unit`, `Null` as return types

- **[03 — Strings and String Interpolation](03-strings-and-interpolation.md)**
  - String literals, multi-line strings, raw strings
  - `s""`, `f""`, `raw""`, `xml""` interpolators
  - Building custom interpolators (the `StringContext` trick)
  - Common String API methods
  - Performance: when to use `StringBuilder`

### Part II — Data and Control

- **[04 — Collections](04-collections.md)**
  - The collection hierarchy (`Iterable`, `Seq`, `Set`, `Map`)
  - Immutable vs mutable; the rule of thumb
  - `List`, `Vector`, `LazyList`, `Array`, `Range`
  - `Set`, `HashSet`, `TreeSet`, `BitSet`
  - `Map`, `HashMap`, `TreeMap`, `ListMap`
  - Common operations: `map`, `flatMap`, `filter`, `fold`, `reduce`, `groupBy`, `partition`, `zip`, `collect`
  - Strict vs lazy collections; `view` and `LazyList`
  - `Iterator` and one-shot traversal
  - `Tuple` (`(a, b)`) and named-tuple alternatives
  - Mutable counterparts: when (rarely) to use them

- **[05 — Pattern Matching](05-pattern-matching.md)**
  - `match` expressions
  - Type, literal, constructor, and tuple patterns
  - Sequence patterns (`List(a, _, c)`, `_*`)
  - Guards (`if`)
  - Variable binding (`@`)
  - Sealed types and exhaustiveness
  - Custom extractors (`unapply`, `unapplySeq`)
  - Partial functions (`PartialFunction`)
  - Regex pattern matching

### Part III — OOP in Scala

- **[06 — Classes and Objects](06-classes-and-objects.md)**
  - `class`, primary constructor, secondary constructors
  - Auxiliary constructors with `this(...)`
  - Fields: `val`, `var`, private/protected
  - Methods, including override and `final`
  - `object` (singletons) and companion objects
  - `apply`, `unapply`, `update`
  - `case class`: synthesized `equals`, `hashCode`, `toString`, `copy`, `apply`, `unapply`
  - `case object`
  - Visibility modifiers: `private`, `protected`, `private[pkg]`

- **[07 — Traits and Inheritance](07-traits-and-inheritance.md)**
  - `trait`: abstract members, concrete members, mixin composition
  - Linearization and the diamond problem
  - `super` in traits (stackable modifications)
  - Self-types (`this: T =>`)
  - Abstract classes vs traits — when to use each
  - `sealed` traits and ADTs
  - **Scala 3 note**: `enum` for ADTs

### Part IV — Type System

- **[08 — Type System Fundamentals](08-type-system.md)**
  - The full type hierarchy (`Any`, `AnyVal`, `AnyRef`, `Nothing`, `Null`, `Unit`)
  - Type aliases (`type X = Y`)
  - Variance: covariance (`+T`), contravariance (`-T`), invariance
  - Upper and lower bounds (`<:`, `>:`)
  - View bounds and context bounds (`A : Ord`)
  - Type ascription vs casting (`asInstanceOf`)
  - **Scala 3 note**: `opaque type`, union types `A | B`, intersection types `A & B`, match types

- **[09 — Generics and Advanced Types](09-advanced-types.md)**
  - Generic classes, methods, and traits
  - F-bounded polymorphism (`A <: Comparable[A]`)
  - Higher-kinded types (`F[_]`, `M[_, _]`)
  - Existential types (Scala 2 only) and their replacement in Scala 3
  - Type lambdas (`({type L[A] = ...})#L` in Scala 2; `[A] =>> ...` in Scala 3)
  - Path-dependent types (`Outer#Inner`, `outer.Inner`)
  - Structural types (duck typing)
  - Refined types

### Part V — Implicits and Contextual Abstractions

- **[10 — Implicits (Scala 2)](10-implicits-scala2.md)**
  - `implicit val`, `implicit def`, `implicit class`, `implicit object`
  - Implicit parameters and resolution rules
  - The implicit scope and where Scala looks
  - Implicit conversions
  - View bounds (`<%`) and context bounds (`:`)
  - Evidence parameters (`A =:= B`, `A <:< B`)
  - The type class encoding with implicits
  - Common pitfalls and how to debug implicit resolution (`-Xlog-implicits`)

- **[11 — Given/Using (Scala 3)](11-given-using-scala3.md)**
  - `given` and `using` — the new spelling
  - `extension` methods
  - `summon[T]` (the new `implicitly`)
  - Given imports (`import x.given`)
  - Implicit conversions in Scala 3 (`Conversion[A, B]`)
  - Migration patterns from Scala 2 implicits
  - When and why Scala 3 split them

### Part VI — Functional Programming

- **[12 — Functional Programming Foundations](12-functional-programming.md)**
  - Pure functions and referential transparency
  - Immutability in practice
  - First-class functions (recap)
  - Recursion and tail recursion (`@tailrec`)
  - Function composition (`andThen`, `compose`)
  - Currying revisited
  - Algebraic Data Types in Scala (sealed traits + case classes; Scala 3 enums)

- **[13 — Error Handling](13-error-handling.md)**
  - `Option[A]` and the absence of `null`
  - `Either[L, R]` for biased error handling
  - `Try[A]` for exception capture
  - `Validated[E, A]` (from Cats) for accumulating errors
  - When exceptions are still appropriate
  - Mapping between them (`Option.toRight`, `Try.toEither`)

- **[14 — Lazy Evaluation](14-lazy-evaluation.md)**
  - `lazy val` semantics (one-time, thread-safe init)
  - By-name parameters (`=> A`) and `Function0`
  - `LazyList` (formerly `Stream`) for infinite sequences
  - Memoization patterns
  - Performance and pitfalls (memory leaks via `LazyList`)

- **[15 — For Comprehensions](15-for-comprehensions.md)**
  - Desugaring `for/yield` into `map`/`flatMap`/`withFilter`
  - Generators, guards, and value definitions
  - For comprehensions over `Option`, `Either`, `Future`, `Try`, `List`
  - Building your own monad that works in for-comprehensions

### Part VII — Type Classes and FP Abstractions

- **[16 — The Type Class Pattern](16-typeclasses.md)**
  - The pattern: trait + implicit instances + summoner
  - Worked example: `Show[A]`, `Eq[A]`, `Ord[A]` from scratch
  - Conditional/derived instances
  - Type class derivation (`Shapeless` in Scala 2, `derives` in Scala 3)
  - Coherence and orphan instances

- **[17 — Functors, Applicatives, Monads, and Friends](17-monads-and-fp-abstractions.md)**
  - `Functor`, `Applicative`, `Monad`, `Traverse`, `Foldable`
  - The laws each must obey
  - Worked examples with `List`, `Option`, `Either`, custom types
  - Why monads don't compose; `MonadTransformer`
  - `Semigroup`, `Monoid`
  - A brief tour of the **Cats** library

### Part VIII — Concurrency and Effects

- **[18 — Futures and Promises](18-concurrency-futures.md)**
  - `Future[A]` and the `ExecutionContext`
  - Combinators: `map`, `flatMap`, `recover`, `recoverWith`, `zip`
  - `Promise[A]` for bridging callback APIs
  - `Await` and why you shouldn't use it
  - `blocking { ... }` for blocking operations
  - Pitfalls: thread starvation, exception swallowing, fairness
  - **Scala 3 note**: `Future` API unchanged; new alternatives like `ox` exist

- **[19 — Cats Effect and the IO Monad](19-cats-effect-and-io.md)**
  - The motivation: `Future` is eager; `IO` is referentially transparent
  - `IO[A]`: building, sequencing, running
  - Resource safety with `Resource[F, A]`
  - Concurrency: fibers, `parMapN`, `race`, `parTraverse`
  - Cancellation
  - Brief mention of **ZIO** as an alternative effect system

- **[20 — Actors and Streams (intro)](20-actors-and-streams.md)**
  - Brief intro to the actor model (Akka / Pekko)
  - Akka Streams basics (`Source`, `Flow`, `Sink`, back-pressure)
  - When to reach for actors vs effects vs streams
  - Pointer to the dedicated Akka post for depth

### Part IX — Metaprogramming and Tooling

- **[21 — Macros and Metaprogramming](21-macros-and-metaprogramming.md)**
  - `inline` (Scala 3) and the simpler metaprogramming model
  - Scala 2 def macros and why they're niche
  - Scala 3 quoted macros (`'{}` and `${}`)
  - Reflection (`scala.reflect`) — when (rarely) to use it
  - Practical examples: compile-time JSON codec generation

- **[22 — Build with sbt](22-build-tools-sbt.md)**
  - `build.sbt` anatomy
  - Tasks vs settings
  - Multi-project builds
  - Common plugins: `sbt-assembly`, `sbt-native-packager`, `scalafmt`, `scalafix`
  - Cross-compiling for Scala 2 and 3
  - Mill and scala-cli as alternatives

- **[23 — Testing](23-testing.md)**
  - **ScalaTest** styles (FunSuite, FlatSpec, WordSpec) and matchers
  - **MUnit** as the lighter modern alternative
  - **ScalaCheck** for property-based testing
  - Mocking: Mockito-Scala, EasyMock, hand-rolled stubs
  - Testing async code (Futures, IO)
  - Testing actors (Akka TestKit)

- **[24 — Java Interop](24-java-interop.md)**
  - Calling Java from Scala — automatic
  - Calling Scala from Java — name mangling, traits, default args
  - Java collections in Scala (`scala.jdk.CollectionConverters`)
  - Java generics and Scala type inference
  - SAM types and Java functional interfaces

### Part X — Putting it together

- **[25 — Best Practices and Idioms](25-best-practices.md)**
  - Idiomatic Scala 2 vs idiomatic Scala 3
  - Naming conventions
  - When to use case classes, when sealed traits, when enums
  - When `Option` becomes overuse
  - When `Future` becomes overuse
  - Effect systems: pick one and stick to it
  - Scalafmt + scalafix as guard rails

- **[26 — Cheatsheet](26-cheatsheet.md)**
  - Syntax-at-a-glance for every concept above
  - Scala 2 → Scala 3 migration table
  - Common idioms one-liners
  - Top stdlib methods you'll use every day

---

## Status

All 27 chapters are complete (v1).

| Chapter | Status |
|---|---|
| 00 — Setup | ✅ done |
| 01 — Language Basics | ✅ done |
| 02 — Functions | ✅ done |
| 03 — Strings | ✅ done |
| 04 — Collections | ✅ done |
| 05 — Pattern Matching | ✅ done |
| 06 — Classes and Objects | ✅ done |
| 07 — Traits | ✅ done |
| 08 — Type System | ✅ done |
| 09 — Advanced Types | ✅ done |
| 10 — Implicits (Scala 2) | ✅ done |
| 11 — Given/Using (Scala 3) | ✅ done |
| 12 — FP Foundations | ✅ done |
| 13 — Error Handling | ✅ done |
| 14 — Lazy Evaluation | ✅ done |
| 15 — For Comprehensions | ✅ done |
| 16 — Type Classes | ✅ done |
| 17 — Functors / Monads / etc | ✅ done |
| 18 — Futures | ✅ done |
| 19 — Cats Effect | ✅ done |
| 20 — Actors and Streams | ✅ done |
| 21 — Macros | ✅ done |
| 22 — sbt | ✅ done |
| 23 — Testing | ✅ done |
| 24 — Java Interop | ✅ done |
| 25 — Best Practices | ✅ done |
| 26 — Cheatsheet | ✅ done |

---

## Conventions used in this guide

- `// scala 2` and `// scala 3` callouts when behavior differs
- `// REPL output:` lines show what you'd see in the REPL
- A `> ...` prompt indicates a shell command
- `Type[A, B]` annotations are explicit even when inference would handle them, because the doc is teaching the type system

## Contributing

This is a personal learning resource. PRs welcome for typos, factual errors, missing examples, or new edge cases.

## License

MIT.
