# Chapter 7 — Traits and Inheritance

> Traits are Scala's most distinctive OOP feature. They're like Java interfaces but with implementations, like mixins from other languages, and the foundation of how Scala does abstraction without multiple inheritance pitfalls.

**In this chapter:**
1. [`trait`: the basics](#1-trait-the-basics)
2. [Mixing traits in](#2-mixing-traits-in)
3. [Stackable trait pattern (`super` in traits)](#3-stackable-trait-pattern-super-in-traits)
4. [Linearization](#4-linearization)
5. [Self-types](#5-self-types)
6. [Abstract classes vs traits](#6-abstract-classes-vs-traits)
7. [Sealed traits and ADTs](#7-sealed-traits-and-adts)
8. [Scala 3 `enum`](#8-scala-3-enum)

---

## 1. `trait`: the basics

```scala
trait Greeter {
  def name: String                 // abstract member
  def greet(): String = s"Hello, $name"   // concrete member
}
```

Traits can hold:
- Abstract `def`s, `val`s, `var`s, `type`s.
- Concrete implementations.
- Constants and helper methods.

A class extends one or more traits with `extends` and `with`:

```scala
class Person(val name: String) extends Greeter

new Person("Bhaskar").greet()    // "Hello, Bhaskar"
```

The first trait is `extends`; subsequent ones are `with`:

```scala
trait Walker { def walk(): String = "walking" }
trait Talker { def talk(): String = "talking" }

class Human extends Walker with Talker

new Human().walk()    // "walking"
new Human().talk()    // "talking"
```

> **Scala 3 note:** Traits can have constructor parameters in Scala 3:
> ```scala
> // scala 3
> trait Greeter(val greeting: String)
> ```
> In Scala 2, you fake this with abstract `val`s.

---

## 2. Mixing traits in

You can mix traits into an *instance*, not just a class:

```scala
trait Logger {
  def log(msg: String): Unit = println(s"[log] $msg")
}

class Service {
  def doWork(): Unit = println("working")
}

val s = new Service with Logger
s.doWork()
s.log("done")
```

This anonymous mixin is useful for one-off enrichments — say, adding logging to a test instance without changing the production class.

---

## 3. Stackable trait pattern (`super` in traits)

A trait can call `super` even though it doesn't know what's "above" it. This enables **stackable** behavior:

```scala
abstract class IntQueue {
  def get(): Int
  def put(x: Int): Unit
}

class BasicIntQueue extends IntQueue {
  private val buf = scala.collection.mutable.ArrayBuffer.empty[Int]
  def get() = buf.remove(0)
  def put(x: Int) = buf += x
}

trait Doubling extends IntQueue {
  abstract override def put(x: Int): Unit = super.put(x * 2)
}

trait Incrementing extends IntQueue {
  abstract override def put(x: Int): Unit = super.put(x + 1)
}

trait Filtering extends IntQueue {
  abstract override def put(x: Int): Unit = if (x >= 0) super.put(x)
}

val q = new BasicIntQueue with Filtering with Incrementing with Doubling
q.put(-1); q.put(0); q.put(1)
List(q.get(), q.get())    // List(2, 4)  (rightmost trait runs first)
```

`abstract override` is the magic incantation: it allows `super.put(x)` even though the parent (`IntQueue`) declares `put` abstract — Scala resolves what `super` means based on the linearization order.

---

## 4. Linearization

When a class `C` mixes in multiple traits with overlapping method definitions, Scala picks a single, deterministic order: the **linearization**.

The rule (simplified): right-to-left traversal of the `extends ... with ...` list, with each type's own ancestors expanded, deduplicated keeping the *rightmost* occurrence.

```scala
class A
trait B extends A
trait C extends A
class D extends B with C

// Linearization of D, left to right:
// D -> C -> B -> A -> AnyRef -> Any
```

Method calls resolve along this list. `super` in `C` refers to *whatever's next* in the linearization (here, `B`).

This is why trait stacking works without ambiguity even when multiple traits define the same method — there's always a single "next" definition, and you can reach it with `super`.

You can inspect linearization via the REPL:

```scala
:type-at MyClass
```

Or by reading the docs / source. For hand-written code, the heuristic *"the rightmost trait wins for method dispatch; everything else is in `super` order"* is enough 95% of the time.

---

## 5. Self-types

A self-type declaration says "this trait can only be mixed into a class that also extends X." It's a way to express dependencies between mixins without inheritance:

```scala
trait UserRepo {
  def findUser(id: String): Option[String]
}

trait UserService {
  this: UserRepo =>      // self-type: requires UserRepo

  def greet(id: String): String =
    findUser(id).map(name => s"hello $name").getOrElse("not found")
}

// won't compile: UserService alone needs UserRepo
// class App extends UserService

class App extends UserService with UserRepo {
  def findUser(id: String) = if (id == "1") Some("Bhaskar") else None
}
```

Self-types let you split implementation across cooperating traits — the so-called **cake pattern** for dependency injection. It's powerful but verbose; modern Scala often prefers passing dependencies as constructor parameters or using implicits/givens.

The `this: T =>` form names the self reference as `this`. You can also rename it:

```scala
trait Foo {
  self: Bar =>     // refers to the same instance as `self`
  def x: Int = self.y    // y comes from Bar
}
```

---

## 6. Abstract classes vs traits

You can use either to define an abstract type. When to choose which:

| Need | Use |
|---|---|
| Multiple inheritance / mixin | `trait` |
| Constructor parameters in Scala 2 | `abstract class` (until Scala 3) |
| Java interop (passing as a Java interface) | `trait` (compiles to interface) |
| Java interop (passing as a Java class) | `abstract class` |
| Heavy state with init logic | `abstract class` (slightly cleaner in Scala 2) |
| Type with no implementation, "interface-like" | `trait` |

Default to `trait`. Reach for `abstract class` only when you need constructor params (Scala 2) or Java interop forces your hand.

---

## 7. Sealed traits and ADTs

A `sealed` trait can only be extended in the same file. This lets the compiler:

- Check exhaustiveness in `match` (covered in [Chapter 5](05-pattern-matching.md)).
- Treat the family as a closed algebraic data type.

```scala
sealed trait Shape
case class Circle(r: Double)              extends Shape
case class Square(side: Double)           extends Shape
case class Rectangle(w: Double, h: Double) extends Shape

def area(s: Shape): Double = s match {
  case Circle(r)         => math.Pi * r * r
  case Square(side)      => side * side
  case Rectangle(w, h)   => w * h
}
```

This is *the* canonical way to express sum types in Scala 2. The compiler can:

- Warn if you forget a case (`match may not be exhaustive`).
- Generate efficient pattern matches.
- Prevent surprise extensions in some other module.

`sealed abstract class` works the same; choose `sealed trait` for ADTs unless you have a specific reason.

### `sealed trait` vs `sealed class`

A `sealed class` can have constructor params and an implementation; a `sealed trait` cannot have constructor params (in Scala 2). For pure ADT use, `sealed trait` is conventional.

---

## 8. Scala 3 `enum`

Scala 3 adds `enum`, which is much shorter:

```scala
// scala 3
enum Shape:
  case Circle(r: Double)
  case Square(side: Double)
  case Rectangle(w: Double, h: Double)

def area(s: Shape): Double = s match
  case Shape.Circle(r)        => math.Pi * r * r
  case Shape.Square(side)     => side * side
  case Shape.Rectangle(w, h)  => w * h
```

For simple enums (no parameters), it's even cleaner:

```scala
// scala 3
enum Day:
  case Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
```

These compile to a sealed hierarchy underneath, but the syntax is dramatically less ceremonial. Scala 2 has nothing equivalent — you write `sealed trait + case classes/objects`.

`enum` cases can also have shared methods on the enum itself:

```scala
// scala 3
enum Status:
  case Active, Inactive, Banned
  def isUsable: Boolean = this match
    case Active => true
    case _      => false
```

---

## What you should now know

- Defining traits with abstract and concrete members.
- Mixing traits into classes (`extends X with Y with Z`) and into instances.
- The stackable trait pattern with `abstract override` and `super`.
- Linearization — that there's always one deterministic order.
- Self-types and how they encode "needs another trait."
- When to choose `trait` vs `abstract class`.
- Sealed traits + case classes / case objects as Scala 2's ADT idiom.
- Scala 3's `enum` syntax as a shorter way to write the same.

The next chapter ([Chapter 8 — Type System Fundamentals](08-type-system.md)) goes into Scala's type system: variance, bounds, and the rest of the lattice.

---

[← Previous: Chapter 6 — Classes and Objects](06-classes-and-objects.md) | [Back to README](README.md) | [Next: Chapter 8 — Type System →](08-type-system.md)
