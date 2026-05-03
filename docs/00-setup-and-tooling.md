# Chapter 00 — Setup and Tooling

> The minimum you need to compile and run Scala on your machine, plus the tools that make it pleasant.

**In this chapter:**
1. [Installing the JDK](#1-installing-the-jdk)
2. [Coursier and `cs setup`](#2-coursier-and-cs-setup)
3. [scala-cli for one-file scripts](#3-scala-cli-for-one-file-scripts)
4. [sbt for real projects](#4-sbt-for-real-projects)
5. [The REPL and worksheets](#5-the-repl-and-worksheets)
6. [IDE: IntelliJ vs Metals](#6-ide-intellij-vs-metals)
7. [Hello World, three ways](#7-hello-world-three-ways)
8. [The shape of a real project](#8-the-shape-of-a-real-project)

---

## 1. Installing the JDK

Scala runs on the JVM, so you need a JDK. **JDK 17 or newer** in 2026.

The easiest path is [SDKMAN!](https://sdkman.io/) on macOS/Linux:

```bash
> curl -s "https://get.sdkman.io" | bash
> sdk install java 21.0.4-tem
```

On Windows: install Temurin from [adoptium.net](https://adoptium.net/) or use [Scoop](https://scoop.sh/).

Verify:

```bash
> java -version
openjdk version "21.0.4" 2024-07-16 LTS
```

---

## 2. Coursier and `cs setup`

[Coursier](https://get-coursier.io/) is the package manager for the Scala ecosystem. `cs setup` installs the JDK, sbt, scala-cli, scalafmt, and the REPL in one go.

```bash
> brew install coursier/formulas/coursier   # macOS
> cs setup                                  # interactive install
```

After that you have:

- `scala` — the REPL and runner
- `scalac` — the compiler
- `sbt` — the build tool
- `scala-cli` — single-file scripting and small projects
- `scalafmt` — the formatter
- `amm` — the Ammonite REPL (richer than the default)

---

## 3. scala-cli for one-file scripts

`scala-cli` is the fastest way to run a quick Scala snippet. No project, no `build.sbt`.

Make a file `Hello.scala`:

```scala
//> using scala 2.13.14

@main def hello(): Unit =
  println("hello from scala-cli")
```

Run:

```bash
> scala-cli run Hello.scala
hello from scala-cli
```

The `//> using` directives let you specify Scala version, dependencies, JVM flags, and more, all in the file:

```scala
//> using scala 2.13.14
//> using dep "org.typelevel::cats-core:2.10.0"

import cats.implicits._

@main def main(): Unit =
  println(List(1, 2, 3).combineAll)   // 6
```

`scala-cli` will fetch Cats and run it. Perfect for blog-post-sized examples or interview prep.

---

## 4. sbt for real projects

[sbt](https://www.scala-sbt.org/) is the standard build tool for serious Scala projects. Generate a starter:

```bash
> sbt new scala/scala-seed.g8
```

That gives you:

```
my-project/
├── build.sbt
├── project/
│   ├── build.properties
│   └── plugins.sbt
└── src/
    ├── main/scala/example/Hello.scala
    └── test/scala/example/HelloSpec.scala
```

Run it:

```bash
> sbt
sbt:my-project> run
sbt:my-project> test
sbt:my-project> compile
sbt:my-project> console      # opens the REPL with your classpath
```

We give sbt its own deep dive in [Chapter 22](22-build-tools-sbt.md). For now, treat it as "the thing that runs `compile`, `test`, and `run`."

---

## 5. The REPL and worksheets

Three interactive options:

**1. The standard REPL (`scala`)** — fast to start, good for quick checks:

```bash
> scala
Welcome to Scala 2.13.14
scala> 1 + 1
res0: Int = 2
scala> :quit
```

`:help` shows commands. Useful ones: `:type expr`, `:paste`, `:reset`, `:load file.scala`.

**2. Ammonite (`amm`)** — a much richer REPL. Multi-line editing, syntax highlighting, builtin dependency resolution:

```bash
> amm
@ import $ivy.`org.typelevel::cats-core:2.10.0`
@ import cats.implicits._
@ List(1, 2, 3).combineAll
res2: Int = 6
```

**3. IDE worksheets** — IntelliJ's `.sc` files and Metals' worksheet feature evaluate code line-by-line and show results in a side panel. Best for stepping through a series of expressions while you learn.

---

## 6. IDE: IntelliJ vs Metals

Two real choices in 2026:

**IntelliJ IDEA + Scala plugin.** Mature, full IDE, the heaviest option. Refactoring, "find usages," debugger, profiler — all polished. Free Community Edition is enough for Scala work. Best for large projects.

**Metals + VS Code (or Neovim, Emacs, Sublime).** A Language Server Protocol implementation. Lighter weight, faster startup, runs anywhere VS Code does. Catches up with IntelliJ on most features and surpasses it on Scala 3 metaprogramming. Best for quick edits, server-class machines, or remote development.

Either is fine. If you don't have a strong preference, try Metals first — it's quicker to set up.

---

## 7. Hello World, three ways

### As a script (scala-cli)

`hello.scala`:

```scala
//> using scala 2.13.14

object Hello {
  def main(args: Array[String]): Unit = println("Hello, world!")
}
```

```bash
> scala-cli run hello.scala
```

### With `@main` (Scala 3, or Scala 2.13+)

```scala
//> using scala 2.13.14

object hello {
  def main(args: Array[String]): Unit = println("Hello!")
}
```

> **Scala 3 note:** `@main` annotation lets you skip the `object` boilerplate:
> ```scala
> // scala 3
> @main def hello(): Unit = println("Hello!")
> ```

### As a real sbt project

`build.sbt`:

```scala
name := "hello"
scalaVersion := "2.13.14"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test
```

`src/main/scala/Hello.scala`:

```scala
object Hello extends App {
  println("Hello!")
}
```

`extends App` is the legacy style; `def main(args: Array[String]): Unit` is the explicit alternative and what most modern code uses.

```bash
> sbt run
```

---

## 8. The shape of a real project

A typical sbt-driven project:

```
my-app/
├── build.sbt                 # main build definition
├── project/
│   ├── build.properties      # which sbt version to use
│   ├── plugins.sbt           # sbt plugins
│   └── Dependencies.scala    # (optional) extracted deps
├── src/
│   ├── main/
│   │   ├── scala/            # production code
│   │   │   └── com/acme/app/...
│   │   └── resources/        # config files (application.conf, logback.xml)
│   └── test/
│       ├── scala/            # test code (mirrors main/)
│       └── resources/        # test fixtures
├── .scalafmt.conf            # formatter config
├── .scalafix.conf            # rewrite-rules config
├── README.md
└── .gitignore                # at minimum: target/, .idea/, .bsp/, .metals/
```

Standard conventions:

- **Package layout** mirrors directory layout (`com.acme.app.OrderService` lives in `src/main/scala/com/acme/app/OrderService.scala`).
- **Test layout** mirrors `main/`. A class `Foo` is tested in `FooSpec` (or `FooTest` — pick one).
- **`application.conf`** for runtime config (Typesafe Config / HOCON).
- **`logback.xml`** if you're using Logback (the most common SLF4J backend).

Everything in `target/` is generated; never check it in.

---

## What you should now know

- A working JDK + Coursier + sbt + scala-cli on your machine.
- Three ways to run Scala: the REPL/Ammonite, scala-cli for one-file experiments, sbt for projects.
- An IDE (IntelliJ or VS Code + Metals) configured to give you autocomplete and inline errors.
- Where production and test code live in a typical project layout.

Now you can experiment with the rest of this guide. Every example will run on the setup above.

---

[← Back to README](README.md) | [Next: Chapter 1 — Language Basics →](01-language-basics.md)
