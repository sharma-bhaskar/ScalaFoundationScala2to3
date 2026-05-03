# Chapter 6 — Classes and Objects

> Scala is a multi-paradigm language. The OOP side is small but powerful: `class`, `object`, and `case class` cover almost everything. This chapter walks through every form of declaration with examples.

**In this chapter:**
1. [`class`: the basic shape](#1-class-the-basic-shape)
2. [Primary constructor](#2-primary-constructor)
3. [Auxiliary constructors](#3-auxiliary-constructors)
4. [Fields: `val` and `var`](#4-fields-val-and-var)
5. [Methods, override, `final`](#5-methods-override-final)
6. [`object`: singletons](#6-object-singletons)
7. [Companion objects](#7-companion-objects)
8. [`apply`, `unapply`, `update`](#8-apply-unapply-update)
9. [`case class`](#9-case-class)
10. [`case object`](#10-case-object)
11. [Visibility modifiers](#11-visibility-modifiers)

---

## 1. `class`: the basic shape

```scala
class Person(val name: String, var age: Int)

val p = new Person("Bhaskar", 30)
p.name        // "Bhaskar"  — read-only because `val`
p.age = 31    // OK — `var`
// p.name = "X"  // compile error
```

`val name` exposes the parameter as an immutable field. `var age` exposes it as a mutable field. With neither, the parameter is private to the constructor.

```scala
class Foo(name: String)             // name not exposed; only constructor sees it
class Bar(val name: String)         // exposed as immutable field
class Baz(var name: String)         // exposed as mutable field
class Qux(private val name: String) // visible in class but not from outside
```

> **Scala 3 note:** Same syntax. `new` is also optional in Scala 3 for class instantiation:
> ```scala
> // scala 3
> val p = Person("Bhaskar", 30)   // no `new`
> ```

---

## 2. Primary constructor

The primary constructor is *the class signature itself*. The class body is the constructor body:

```scala
class Counter(start: Int) {
  println(s"Counter created with start=$start")    // runs at construction
  private var n = start

  def value: Int = n
  def incr(): Unit = n += 1
}

val c = new Counter(10)
// "Counter created with start=10"
c.value      // 10
c.incr()
c.value      // 11
```

Anything you put in the class body — field initializations, side effects, helper methods — runs at construction time.

---

## 3. Auxiliary constructors

Defined as `def this(...)`. Every auxiliary constructor must, on its first line, call another constructor (auxiliary or primary):

```scala
class Person(val name: String, val age: Int) {
  def this(name: String) = this(name, 0)                        // age defaults to 0
  def this()              = this("anonymous")                   // chains through above
}

new Person("Bhaskar", 30)
new Person("Bhaskar")
new Person()
```

In practice, default arguments on the primary constructor often replace auxiliary constructors:

```scala
class Person(val name: String = "anonymous", val age: Int = 0)

new Person()
new Person("Bhaskar")
new Person("Bhaskar", 30)
new Person(age = 30)
```

---

## 4. Fields: `val` and `var`

Inside a class body:

```scala
class Account {
  val openedAt: java.time.Instant = java.time.Instant.now()  // immutable
  var balance: Double = 0.0                                  // mutable
  private val auditLog: List[String] = Nil                   // hidden
  protected var lastTouched: Long = 0L                       // visible to subclasses
}
```

`val` fields produce a getter only. `var` fields produce a getter and a setter named `field_=`:

```scala
class Box {
  var contents: String = ""
}

val b = new Box
b.contents = "hello"        // calls b.contents_= ("hello")
println(b.contents)         // calls b.contents
```

You can override the implicit getter/setter for custom logic:

```scala
class Bounded {
  private var _n: Int = 0
  def n: Int = _n
  def n_=(v: Int): Unit = _n = math.max(0, math.min(100, v))
}

val b = new Bounded
b.n = 150     // calls n_=, clamps to 100
b.n           // 100
```

---

## 5. Methods, override, `final`

```scala
class Animal {
  def speak(): String = "(silence)"
  def name: String    = "animal"
}

class Dog extends Animal {
  override def speak(): String = "woof"      // override required
  override val name: String    = "dog"        // can override def with val
}

new Dog().speak()    // "woof"
new Dog().name       // "dog"
```

The `override` keyword is required when overriding non-abstract members. It catches typos at compile time.

`final` prevents further override:

```scala
class Animal {
  final def species: String = "Animalia"
}

class Dog extends Animal {
  // override def species: String = "Dog"   // compile error: cannot override final
}
```

A `final class` cannot be extended at all.

---

## 6. `object`: singletons

`object` declares a singleton — a class with exactly one instance, accessed by name:

```scala
object Logger {
  private var counter = 0
  def log(msg: String): Unit = {
    counter += 1
    println(s"[$counter] $msg")
  }
}

Logger.log("hello")
Logger.log("world")
// [1] hello
// [2] world
```

Common uses:

- **Utility / helper functions** without a class to attach them to.
- **Companion objects** for classes (next section).
- **Module-style namespacing** for related constants and methods.
- **Type classes** instances (later chapters).

`object`s are lazy: they're initialized on first access, not at JVM startup.

---

## 7. Companion objects

When an `object` and a `class` (or `trait`) share a name and live in the *same file*, they are **companions**. Companions can see each other's `private` members.

```scala
class Account private (val id: String, var balance: Double) {
  override def toString = s"Account($id, $$$balance)"
}

object Account {
  def open(id: String): Account = new Account(id, 0.0)        // factory
  def transfer(from: Account, to: Account, amount: Double): Unit = {
    from.balance -= amount
    to.balance   += amount
  }
}

val a = Account.open("A-1")    // factory call
val b = Account.open("A-2")
Account.transfer(a, b, 50.0)
```

Notice `class Account private` — the constructor is private. Callers must go through the companion's `open` factory. This is a common idiom for constructing values with validation or invariants.

A companion can hold:

- Factory methods.
- Extractor methods (`unapply`).
- Constants and shared resources.
- Type class instances (in FP idioms).
- Implicit/given values defined for the type.

---

## 8. `apply`, `unapply`, `update`

Three method names with special syntactic sugar.

### `apply`

`X(args)` calls `X.apply(args)`. This works for both classes/instances and objects:

```scala
class Adder(by: Int) {
  def apply(x: Int): Int = x + by
}

val plus5 = new Adder(5)
plus5(10)        // 15  — sugar for plus5.apply(10)
```

Most commonly used in companion objects to enable `MyClass(args)` factory syntax without `new`:

```scala
class Vec(val x: Double, val y: Double)
object Vec {
  def apply(x: Double, y: Double): Vec = new Vec(x, y)
}

Vec(1.0, 2.0)    // sugar for Vec.apply(1.0, 2.0) — returns new Vec
```

`case class` synthesizes `apply` for you (next section).

### `unapply`

The companion to `apply` for pattern matching, covered in [Chapter 5](05-pattern-matching.md). `case class` synthesizes this too.

### `update`

`x(args) = value` calls `x.update(args, value)`:

```scala
class Grid(rows: Int, cols: Int) {
  private val data = Array.ofDim[Int](rows, cols)

  def apply(r: Int, c: Int): Int               = data(r)(c)
  def update(r: Int, c: Int, v: Int): Unit     = data(r)(c) = v
}

val g = new Grid(3, 3)
g(1, 1) = 42         // sugar for g.update(1, 1, 42)
g(1, 1)              // 42
```

This is what makes `array(i) = x` and `map("key") = value` work.

---

## 9. `case class`

A `case class` is a regular class with a bunch of useful machinery synthesized:

```scala
case class Person(name: String, age: Int)
```

You get for free:

- A factory method (`apply`), so `Person(...)` works without `new`.
- An extractor (`unapply`), so it works in pattern matches.
- Field accessors as `val` by default.
- Sensible `equals`, `hashCode`, and `toString`.
- A `copy` method for non-destructive updates.
- `productArity`, `productIterator`, `productElement` — useful for generic code.

```scala
val p1 = Person("Bhaskar", 30)
val p2 = Person("Bhaskar", 30)

p1 == p2            // true  — value equality
p1.toString         // "Person(Bhaskar,30)"

val p3 = p1.copy(age = 31)    // new Person with age changed
p3                  // Person(Bhaskar,31)

p1 match {
  case Person(name, _) => println(s"matched $name")
}
```

The `copy` method takes named parameters that default to the current values — perfect for "change one thing" updates of immutable data.

`case class` is the canonical way to define data in Scala. Use it whenever you want a value object: requests, responses, events, configuration, ADT variants.

---

## 10. `case object`

A singleton with case-class behavior — proper `toString`, `equals`, `hashCode`. Used heavily in ADTs:

```scala
sealed trait Day
case object Monday    extends Day
case object Tuesday   extends Day
case object Wednesday extends Day
case object Thursday  extends Day
case object Friday    extends Day
case object Saturday  extends Day
case object Sunday    extends Day

def isWeekend(d: Day): Boolean = d match {
  case Saturday | Sunday => true
  case _                  => false
}
```

> **Scala 3 note:** Scala 3 prefers `enum` for closed sets like this:
> ```scala
> // scala 3
> enum Day:
>   case Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
> ```

---

## 11. Visibility modifiers

```scala
class Bank {
  val publicId: String = "..."          // visible everywhere

  private val secret: String = "..."     // visible only inside this Bank instance
  private[this] val verySecret: String   // visible only to *this* instance, not other Bank instances

  protected val familySecret: String    // visible to subclasses too

  private[bank] val pkgSecret: String   // visible inside the `bank` package

  def operation(other: Bank): Unit = {
    println(this.secret)                // OK
    // println(other.verySecret)        // compile error: instance-private
    println(other.secret)               // OK — `private` allows other Bank instances
  }
}
```

The four scopes:

- **`private`** — same class (any instance).
- **`private[this]`** — *just this instance*; even another Bank can't see it.
- **`protected`** — class + subclasses (no other instances unless they're subclasses).
- **`private[some.pkg]`** — visible inside the named package.
- **(default)** — public.

Most code uses `private` and the implicit public default. `private[this]` is rare but useful for lock-style scenarios. `private[somePkg]` is good for "publish to the rest of the module, hide from outside callers."

> **Scala 3 note:** `private[this]` is deprecated in Scala 3 — Scala 3's `private` is already this-instance by default in the cases that matter.

---

## What you should now know

- Every form of class declaration: primary constructor params, auxiliary constructors, defaulted args.
- The difference between `val` parameter, `var` parameter, plain parameter, and class-body field.
- How `override` and `final` interact with inheritance.
- That `object` is a singleton and what makes a *companion* special.
- The three magic method names: `apply`, `unapply`, `update`, and the syntax they enable.
- What `case class` synthesizes for you, and why it's the default for data types.
- The full set of visibility modifiers.

The next chapter ([Chapter 7 — Traits and Inheritance](07-traits-and-inheritance.md)) covers Scala's most distinctive OOP feature: traits, mixin composition, and the linearization that makes them all work.

---

[← Previous: Chapter 5 — Pattern Matching](05-pattern-matching.md) | [Back to README](README.md) | [Next: Chapter 7 — Traits →](07-traits-and-inheritance.md)
