# Chapter 22 — Build with sbt

> sbt is Scala's de-facto build tool. It's powerful and idiosyncratic. This chapter walks the parts you'll actually use.

**In this chapter:**
1. [`build.sbt` anatomy](#1-buildsbt-anatomy)
2. [Tasks vs settings](#2-tasks-vs-settings)
3. [Adding dependencies](#3-adding-dependencies)
4. [Multi-project builds](#4-multi-project-builds)
5. [Common plugins](#5-common-plugins)
6. [Cross-compiling Scala 2 + 3](#6-cross-compiling-scala-2--3)
7. [Mill and scala-cli as alternatives](#7-mill-and-scala-cli-as-alternatives)

---

## 1. `build.sbt` anatomy

A minimal build:

```scala
// build.sbt
ThisBuild / scalaVersion := "2.13.14"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "my-app",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core"   % "2.10.0",
      "org.scalatest" %% "scalatest"   % "3.2.18" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-Wunused:all"
    )
  )
```

Key conventions:

- `ThisBuild /` applies to all projects in the build.
- `Test` configuration (capital T) scopes to the test classpath.
- `%%` adds the Scala binary version (e.g. `cats-core_2.13`); `%` is plain Java deps.
- The DSL is real Scala — you can write helpers, conditionals, etc.

The `project/` directory holds:

- `build.properties` — sbt version (`sbt.version=1.10.0`).
- `plugins.sbt` — sbt plugins.
- Optional `Dependencies.scala`, `Settings.scala` for shared configuration.

---

## 2. Tasks vs settings

Two kinds of "things" in sbt:

- **Setting**: computed once when the build is loaded. `name`, `scalaVersion`, `libraryDependencies`.
- **Task**: re-evaluated every time you ask for it. `compile`, `test`, `run`, `package`.

In the DSL, settings use `:=`, tasks use the same `:=` but the right side has effects:

```scala
// setting
scalaVersion := "2.13.14"

// task
val hello = taskKey[Unit]("greeting")
hello := { println("hi"); () }
```

Tasks can depend on tasks/settings:

```scala
val showVersion = taskKey[Unit]("show version")
showVersion := { println(version.value) }
```

Use `.value` inside a task body — it triggers evaluation of the dependency.

---

## 3. Adding dependencies

```scala
libraryDependencies ++= Seq(
  // managed dependency from a Maven repo
  "org.typelevel" %% "cats-core"      % "2.10.0",
  "org.typelevel" %% "cats-effect"    % "3.5.4",

  // test only
  "org.scalatest" %% "scalatest"      % "3.2.18" % Test,

  // Java-only library (single %)
  "ch.qos.logback" % "logback-classic" % "1.5.6"
)
```

`%%` — Scala dependency, sbt appends `_2.13` (or whichever) automatically.
`%` — plain Maven artifact (Java libs).
`% Test` — only on the test classpath.

Add a custom resolver if you need a non-Maven repo:

```scala
resolvers += "Sonatype Snapshots".at("https://oss.sonatype.org/content/repositories/snapshots/")
```

---

## 4. Multi-project builds

Most real apps split into modules:

```scala
ThisBuild / scalaVersion := "2.13.14"

lazy val core = (project in file("core"))
  .settings(name := "myapp-core")

lazy val api = (project in file("api"))
  .dependsOn(core)
  .settings(
    name := "myapp-api",
    libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.3"
  )

lazy val workers = (project in file("workers"))
  .dependsOn(core)
  .settings(name := "myapp-workers")

lazy val root = (project in file("."))
  .aggregate(core, api, workers)   // sbt will compile/test all three
```

Layout:

```
my-app/
├── build.sbt
├── core/src/main/scala/...
├── api/src/main/scala/...
└── workers/src/main/scala/...
```

`dependsOn` adds compile dependencies; `aggregate` is for "run this command across all sub-projects."

---

## 5. Common plugins

In `project/plugins.sbt`:

```scala
// running tests, packaging, formatting, common chores
addSbtPlugin("com.github.sbt"        % "sbt-native-packager"  % "1.10.4")
addSbtPlugin("com.eed3si9n"          % "sbt-assembly"         % "2.2.0")
addSbtPlugin("org.scalameta"         % "sbt-scalafmt"         % "2.5.2")
addSbtPlugin("ch.epfl.scala"         % "sbt-scalafix"         % "0.12.1")
addSbtPlugin("com.github.sbt"        % "sbt-release"          % "1.4.0")
addSbtPlugin("org.scoverage"         % "sbt-scoverage"        % "2.0.12")
```

Highlights:

- **sbt-assembly**: build a fat JAR (`sbt assembly`).
- **sbt-native-packager**: build Docker images, RPMs, .deb, system services.
- **sbt-scalafmt**: format code; also a CLI tool. Configure in `.scalafmt.conf`.
- **sbt-scalafix**: rewrite-rules engine for migrations and lint. Configure in `.scalafix.conf`.
- **sbt-scoverage**: code coverage. `sbt clean coverage test coverageReport`.
- **sbt-release**: release versioning workflow.

Don't enable plugins you won't use — each adds tasks to the build and slows things down.

---

## 6. Cross-compiling Scala 2 + 3

If you're publishing a library, you may want the same code to compile against both Scala 2.13 and Scala 3:

```scala
ThisBuild / crossScalaVersions := Seq("2.13.14", "3.3.3")
ThisBuild / scalaVersion       := "3.3.3"

lazy val mylib = (project in file("."))
  .settings(
    name := "my-lib",
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) => Seq("-Xsource:3")
      case _            => Seq.empty
    })
  )

// build for both
// > sbt +compile +test +publishLocal
```

The `+` prefix runs a task for every cross version. `-Xsource:3` makes Scala 2.13 accept some Scala 3 syntax for ease of migration.

For application code (not a library), pick one Scala version — cross-compilation isn't worth the complexity.

---

## 7. Mill and scala-cli as alternatives

**Mill** ([mill-build.com](https://mill-build.com)) is a faster, simpler Scala build tool. Configuration is a Scala file with case classes and methods. Faster startup, parallel by default.

```scala
// build.sc (Mill)
import mill._, scalalib._

object myapp extends ScalaModule {
  def scalaVersion = "2.13.14"
  def ivyDeps = Agg(ivy"org.typelevel::cats-core:2.10.0")
}
```

If you're starting fresh and don't need extensive sbt-only plugins, Mill is worth a look.

**scala-cli** is for *small* projects. Single file? A handful? Scripts? `scala-cli` doesn't need a build file at all:

```scala
//> using scala 2.13.14
//> using dep "org.typelevel::cats-core:2.10.0"

@main def main(): Unit = println("hi")
```

```bash
> scala-cli run main.scala
```

Use scala-cli for quick experiments and one-file utilities. Use sbt or Mill once you have multi-module needs.

---

## What you should now know

- The structure of `build.sbt` and the `project/` directory.
- Settings vs tasks.
- Adding dependencies with `%%` (Scala) and `%` (Java).
- Multi-project layout with `dependsOn` and `aggregate`.
- Common plugins worth knowing about.
- Cross-compiling Scala 2 and 3 for libraries.
- Mill and scala-cli as alternatives.

The next chapter ([Chapter 23 — Testing](23-testing.md)) covers the testing tooling.

---

[← Previous: Chapter 21 — Macros](21-macros-and-metaprogramming.md) | [Back to README](README.md) | [Next: Chapter 23 — Testing →](23-testing.md)
