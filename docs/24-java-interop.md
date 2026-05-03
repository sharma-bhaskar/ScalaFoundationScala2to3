# Chapter 24 — Java Interop

> Scala compiles to JVM bytecode and shares a runtime with Java. Calling Java from Scala is effortless. Exposing Scala to Java requires more thought.

**In this chapter:**
1. [Calling Java from Scala](#1-calling-java-from-scala)
2. [Java collections in Scala](#2-java-collections-in-scala)
3. [Calling Scala from Java](#3-calling-scala-from-java)
4. [SAM types and Java functional interfaces](#4-sam-types-and-java-functional-interfaces)
5. [Annotations and `@varargs`, `@BeanProperty`](#5-annotations-and-varargs-beanproperty)
6. [Exception handling across the boundary](#6-exception-handling-across-the-boundary)

---

## 1. Calling Java from Scala

Largely seamless. Just import and use:

```scala
import java.util.{ArrayList, HashMap}
import java.time.LocalDate

val list = new ArrayList[Integer]()
list.add(1); list.add(2); list.add(3)

val today = LocalDate.now()
val parsed = LocalDate.parse("2026-05-03")
```

A few translations to keep in mind:

- Java primitives (`int`, `boolean`) ≡ Scala value types (`Int`, `Boolean`) — they auto-box where needed.
- Java's `null` is allowed for any `AnyRef`. (Use `Option(value)` to wrap safely.)
- Java's checked exceptions don't exist in Scala — they compile, but the compiler doesn't enforce `throws` declarations.

Scala uses operators where Java uses methods:

```scala
val sum: Int = Integer.valueOf(2) + Integer.valueOf(3)    // unboxes
```

---

## 2. Java collections in Scala

Don't use Java's `ArrayList`/`HashMap` from Scala code if you don't have to — Scala's collections are nicer. To bridge:

### Scala 2.13+ / Scala 3:

```scala
import scala.jdk.CollectionConverters._

// Java -> Scala
val javaList: java.util.List[Int] = ???
val scalaList: List[Int] = javaList.asScala.toList

val javaMap: java.util.Map[String, Int] = ???
val scalaMap: Map[String, Int] = javaMap.asScala.toMap

// Scala -> Java
val scalaList: List[Int] = List(1, 2, 3)
val javaList: java.util.List[Int] = scalaList.asJava

val scalaMap: Map[String, Int] = Map("a" -> 1)
val javaMap: java.util.Map[String, Int] = scalaMap.asJava
```

The `.asScala` / `.asJava` methods are wrappers — they don't copy. Mutating one mutates the other (where allowed). For a deep copy, follow `.asScala.toList` / `.toMap` etc.

### `Optional` <-> `Option`

Java 8+ has `Optional[T]`. Scala has `Option[T]`. Convert:

```scala
import scala.jdk.OptionConverters._

val javaOpt: java.util.Optional[String] = ???
val scalaOpt: Option[String] = javaOpt.toScala

val s: Option[Int] = Some(42)
val j: java.util.Optional[Int] = s.toJava
```

### `CompletableFuture` <-> `Future`

```scala
import scala.jdk.FutureConverters._

val cf: java.util.concurrent.CompletableFuture[String] = ???
val sf: scala.concurrent.Future[String] = cf.asScala

val sf2: scala.concurrent.Future[Int] = ???
val cf2 = sf2.asJava
```

---

## 3. Calling Scala from Java

You can, but it requires understanding what Scala generates.

### Object members are accessed via `MODULE$` (or via static forwarders)

```scala
// Scala
object Greeter {
  def hello(name: String): String = s"hello $name"
}
```

```java
// Java — two ways
String s1 = Greeter$.MODULE$.hello("Bhaskar");
String s2 = Greeter.hello("Bhaskar");   // static forwarder
```

The compiler synthesizes a static forwarder on a Java-style class for each method on the object — usable directly from Java.

### Scala traits become interfaces with default methods (Scala 2.12+)

```scala
trait Animal {
  def speak(): String = "...silence..."
}

class Dog extends Animal {
  override def speak(): String = "woof"
}
```

Java sees `Animal` as an interface with a default method `speak()`. `Dog` is a regular class implementing it.

### `case class` `apply` requires `MODULE$`

```scala
case class Person(name: String, age: Int)
```

```java
Person p = Person$.MODULE$.apply("Bhaskar", 30);    // factory
// or
Person p = new Person("Bhaskar", 30);                // constructor works too
```

### Fields and getters

A `val` in Scala compiles to a private field plus a getter. Java callers should use the getter:

```scala
class Foo {
  val name: String = "X"
}
```

```java
Foo f = new Foo();
String s = f.name();    // call the getter
```

`var` adds a setter `name_$eq(value)`. Use `@BeanProperty` if you want JavaBean-style `getName` / `setName` (next section).

---

## 4. SAM types and Java functional interfaces

Java 8 introduced functional interfaces (single abstract method). Scala 2.12+ supports passing a Scala lambda directly as a SAM:

```scala
// Java
interface Validator {
  boolean validate(String s);
}

// Scala
val v: Validator = (s: String) => s.length > 0    // lambda becomes a Validator

v.validate("hi")    // true
```

This works for `Runnable`, `Consumer<T>`, `Function<T, R>`, all of `java.util.function.*`, and your own SAM interfaces.

---

## 5. Annotations and `@varargs`, `@BeanProperty`

Two compiler annotations make Scala APIs friendlier from Java.

### `@varargs`

A Scala method with `*` (varargs) compiles to taking a `Seq` parameter. To also expose Java-style varargs, add `@varargs`:

```scala
import scala.annotation.varargs

class Logger {
  @varargs def log(parts: String*): Unit = parts.foreach(println)
}
```

Now Java can call `logger.log("a", "b", "c")` with native varargs syntax.

### `@BeanProperty`

Generates JavaBean-style `getX` / `setX` accessors:

```scala
import scala.beans.BeanProperty

class User(@BeanProperty var name: String, @BeanProperty var age: Int)
```

```java
User u = new User("X", 30);
u.getName();    // "X"
u.setName("Y");
```

Useful for serialization libraries (Jackson, etc.) that expect JavaBean conventions.

### `@throws`

If you want Scala to declare a `throws` clause that Java sees as checked:

```scala
import scala.annotation.throws

class IO {
  @throws(classOf[java.io.IOException])
  def read(): String = ???
}
```

Java callers will be required to catch or declare `IOException`.

---

## 6. Exception handling across the boundary

- Scala doesn't enforce checked exceptions, but the JVM still throws them.
- Java catches `Throwable`, `RuntimeException`, etc., uniformly across both languages.
- Be aware: a checked exception thrown from Scala that crosses to Java without a declared throws clause is technically a JVM contract violation, but in practice it works.

For pure FP code, prefer typed errors (`Either`, `Try`) over throws. At the Java boundary, convert as needed:

```scala
// expose to Java in a friendly way
class JavaFriendlyApi(svc: ScalaService) {
  @throws(classOf[Exception])
  def doWork(input: String): String =
    svc.run(input) match {
      case Right(v) => v
      case Left(e)  => throw new Exception(e)
    }
}
```

---

## What you should now know

- Calling Java from Scala is effortless; just import and use.
- `scala.jdk.CollectionConverters.*` for `.asScala` / `.asJava` bridges.
- `OptionConverters` and `FutureConverters` for the modern Java types.
- Java sees Scala objects via `MODULE$` (or static forwarders) and traits as Java interfaces.
- SAM types let Scala lambdas pass as Java functional interfaces.
- `@varargs` and `@BeanProperty` make Scala APIs nicer for Java callers.
- Be deliberate about exceptions at the boundary.

The next chapter ([Chapter 25 — Best Practices](25-best-practices.md)) is opinionated guidance for writing good Scala.

---

[← Previous: Chapter 23 — Testing](23-testing.md) | [Back to README](README.md) | [Next: Chapter 25 — Best Practices →](25-best-practices.md)
