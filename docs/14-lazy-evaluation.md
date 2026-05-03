# Chapter 14 — Lazy Evaluation

> Three Scala features defer computation: `lazy val`, by-name parameters, and `LazyList`. Understanding the differences keeps you out of memory leaks and accidental re-evaluation.

**In this chapter:**
1. [`lazy val` semantics](#1-lazy-val-semantics)
2. [By-name parameters revisited](#2-by-name-parameters-revisited)
3. [`Function0` (`() => A`) — the explicit thunk](#3-function0----a--the-explicit-thunk)
4. [`LazyList` for infinite sequences](#4-lazylist-for-infinite-sequences)
5. [Memoization patterns](#5-memoization-patterns)
6. [Pitfalls: memory leaks and ordering](#6-pitfalls-memory-leaks-and-ordering)

---

## 1. `lazy val` semantics

A `lazy val` is initialized **on first access**, and exactly once:

```scala
class Service {
  lazy val expensiveResource: Resource = {
    println("opening...")
    Resource.open()
  }

  def use(): Unit = {
    println(expensiveResource.read())
    println(expensiveResource.read())
  }
}

val s = new Service
println("created service")
s.use()

// created service
// opening...
// (read 1)
// (read 2)
```

Two guarantees:
- **Lazy** — not initialized until first access.
- **One-time** — initialized at most once, even from multiple threads.

The compiler generates a flag plus double-checked locking; calls after init are essentially free.

Use cases:
- Expensive resources you might not need.
- Mutually recursive definitions (where eager evaluation would be a forward reference).
- Breaking dependency cycles in initialization.

```scala
class Cache {
  // Without `lazy val`, mutual reference fails — `b` doesn't exist when `a` is constructed.
  lazy val a: Int = b + 1
  lazy val b: Int = 10
}
```

> **Caution:** A `lazy val` adds a small overhead per access (the synchronized check). Don't lazy-val every val. Use it when laziness matters.

> **Scala 3 note:** Same syntax. Scala 3 made `lazy val` slightly cheaper internally.

---

## 2. By-name parameters revisited

`=> A` makes a parameter "evaluated at use, not at call":

```scala
def myAssert(cond: Boolean, msg: => String): Unit =
  if (!cond) throw new AssertionError(msg)

myAssert(true, expensiveString())   // expensiveString never runs
myAssert(false, expensiveString())  // expensiveString runs once (when msg is referenced)
```

A by-name parameter is **re-evaluated on every reference**:

```scala
def runTwice(action: => Int): Int = action + action

var counter = 0
runTwice { counter += 1; counter }    // 1 + 2 = 3 — `action` ran twice
```

If you want a by-name parameter computed at most once, combine with `lazy val`:

```scala
def runOnceLazy(action: => Int): Int = {
  lazy val v = action
  v + v
}

var counter = 0
runOnceLazy { counter += 1; counter }   // counter = 1; result = 1 + 1 = 2
```

This is a tiny but important pattern when designing DSLs that take blocks.

---

## 3. `Function0` (`() => A`) — the explicit thunk

A by-name `=> A` is implicit; `() => A` (a `Function0`) is explicit. The function value:

```scala
val thunk: () => Int = () => {
  println("computing")
  42
}

thunk()    // prints, returns 42
thunk()    // prints again
```

Conversion: `() => A` has a `.apply()` method. `=> A` doesn't — you reference it like a regular value:

```scala
def withByName(x: => Int): Int = x + x       // re-evaluates x twice
def withFunc0(x: () => Int): Int = x() + x() // explicit; you can also do x() + 10 etc.
```

When designing an API, by-name is usually nicer for the caller (no `()` boilerplate). `Function0` is nicer when you want to *pass the thunk around* without evaluating it — you can store it in a `val` and call later.

---

## 4. `LazyList` for infinite sequences

`LazyList` is the lazy version of `List`. Each element is computed on demand and **memoized** (cached):

```scala
val naturals: LazyList[Int] = LazyList.from(1)

naturals.take(5).toList      // List(1, 2, 3, 4, 5)
naturals.head                 // 1
```

Build a `LazyList` from a recursive definition:

```scala
val fibs: LazyList[BigInt] =
  BigInt(0) #:: BigInt(1) #:: fibs.zip(fibs.tail).map { case (a, b) => a + b }

fibs.take(10).toList
// List(0, 1, 1, 2, 3, 5, 8, 13, 21, 34)
```

`#::` is the lazy cons operator (right-associative). The right side isn't evaluated until you access it.

Powerful for stream-like producers:

```scala
def primes: LazyList[Int] = {
  def sieve(s: LazyList[Int]): LazyList[Int] =
    s.head #:: sieve(s.tail.filter(_ % s.head != 0))
  sieve(LazyList.from(2))
}

primes.take(10).toList    // List(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
```

> **Scala 2 note:** `LazyList` was introduced in Scala 2.13, replacing the older `Stream`. `Stream` had subtly different semantics (head-strict).

### `LazyList` vs `Iterator`

Both are lazy. The difference:

- **`LazyList`** memoizes. Once you've computed an element, it stays. Useful when you need to re-traverse.
- **`Iterator`** doesn't memoize. It's one-shot. Lower memory if you don't need to revisit.

For genuinely huge / infinite producers where memory matters, `Iterator` is safer. `LazyList` is better when memoization is the point (e.g., the Fibonacci self-reference above).

---

## 5. Memoization patterns

Caching expensive results keyed by inputs.

### Memoize with mutable map

```scala
import scala.collection.mutable

def memoize[A, B](f: A => B): A => B = {
  val cache = mutable.Map.empty[A, B]
  a => cache.getOrElseUpdate(a, f(a))
}

val slowSquare: Int => Int = n => { Thread.sleep(100); n * n }
val fastSquare = memoize(slowSquare)

fastSquare(5)    // ~100ms
fastSquare(5)    // instant (cache hit)
```

The result is not thread-safe. For multi-threaded use, wrap in a `concurrent.TrieMap` or use a real cache library (Caffeine, Guava cache via Scala wrappers).

### Memoize a recursive function

For recursion (Fibonacci), the trick is memoizing the *recursive* version:

```scala
def fib: Int => BigInt = {
  val cache = mutable.Map.empty[Int, BigInt]
  def go(n: Int): BigInt =
    cache.getOrElseUpdate(n,
      if (n < 2) BigInt(n) else go(n - 1) + go(n - 2)
    )
  go
}

val f = fib
f(100)    // 354224848179261915075
```

For purely functional memoization, look at libraries like **scalaz-memoize** or build your own with a state monad.

---

## 6. Pitfalls: memory leaks and ordering

### `LazyList` head retention

A `LazyList` that's iterated holds onto computed elements. If you keep a reference to the *head*, you can't free anything:

```scala
val all = LazyList.from(1).map(_ * 2)    // infinite!

all.foreach(println)    // never returns; also keeps every computed element alive
```

The fix: don't hold the head. Process and discard:

```scala
LazyList.from(1).map(_ * 2).take(1000).foreach(println)    // only 1000 retained
// or use Iterator for true streaming.
```

### Initialization order

Mixing `lazy val` and `val` in classes can lead to surprising init orders:

```scala
class A {
  val x: Int = compute()
  lazy val y: Int = z + 1     // forward reference!
  val z: Int = 10

  def compute(): Int = z * 2  // z is 0 here! (not yet initialized)
}

new A().x    // 0 — surprising
```

`val` initialization runs in source order. `compute()` reads `z` before it has its real value. The fix: make depended-on fields `lazy val` or reorder.

### Thread-safety claim

`lazy val` is thread-safe for *initialization* (the val is set exactly once). Reads after init don't synchronize, so subsequent updates (none, in `lazy val`) are fine. **Don't confuse this with thread-safety of the value's contents** — if your `lazy val` is a mutable collection, you're responsible for sync on operations against it.

---

## What you should now know

- `lazy val`: initialized on first access, exactly once, thread-safe.
- By-name parameters re-evaluate on every reference; combine with `lazy val` for at-most-once.
- `Function0` (`() => A`) for explicit thunks you want to pass without evaluating.
- `LazyList` for recursive/infinite sequences with memoization; `Iterator` for one-shot streams.
- Memoization patterns with mutable caches.
- The two big pitfalls: holding the head of an infinite `LazyList`, and `lazy val` ordering surprises.

The next chapter ([Chapter 15 — For Comprehensions](15-for-comprehensions.md)) shows how `for/yield` desugars to `flatMap`/`map`/`withFilter` and works on any monad.

---

[← Previous: Chapter 13 — Error Handling](13-error-handling.md) | [Back to README](README.md) | [Next: Chapter 15 — For Comprehensions →](15-for-comprehensions.md)
