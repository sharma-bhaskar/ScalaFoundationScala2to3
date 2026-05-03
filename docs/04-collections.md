# Chapter 4 — Collections

> Scala's collection library is one of its biggest assets — and one of the most overwhelming first looks. This chapter is the comprehensive tour: the hierarchy, the concrete types, the operations, and how to choose between them.

**In this chapter:**
1. [The collection hierarchy](#1-the-collection-hierarchy)
2. [Immutable vs mutable](#2-immutable-vs-mutable)
3. [`List`](#3-list)
4. [`Vector`](#4-vector)
5. [`Array`](#5-array)
6. [`Range`](#6-range)
7. [`LazyList`](#7-lazylist)
8. [`Set`](#8-set)
9. [`Map`](#9-map)
10. [Tuples](#10-tuples)
11. [Common operations](#11-common-operations)
12. [`view` for laziness](#12-view-for-laziness)
13. [`Iterator`](#13-iterator)
14. [Mutable collections](#14-mutable-collections)
15. [Choosing a collection](#15-choosing-a-collection)

---

## 1. The collection hierarchy

The simplified picture:

```
                    Iterable
                   /    |    \
                Seq    Set    Map
                / \           |
            IndexedSeq  LinearSeq
              / | \         |
        Vector Array       List
              ...
```

- **`Iterable[A]`** — anything you can iterate. The root.
- **`Seq[A]`** — ordered, indexed-or-iterable elements. Allows duplicates.
  - **`IndexedSeq[A]`** — fast random access (`xs(i)` is O(log n) or O(1)). Examples: `Vector`, `Array`, `String`.
  - **`LinearSeq[A]`** — fast `head`/`tail`, slow random access. Examples: `List`, `LazyList`.
- **`Set[A]`** — unique elements, no order.
- **`Map[K, V]`** — key-value pairs, unique keys.

Each one comes in *immutable* and *mutable* flavors. The default is immutable; that's what you import as `List`, `Vector`, `Set`, `Map` without qualification.

```scala
import scala.collection.immutable.List   // already the default
import scala.collection.mutable.ListBuffer
```

---

## 2. Immutable vs mutable

```scala
val xs = List(1, 2, 3)
val ys = xs :+ 4              // creates a new List; xs unchanged
// xs == List(1, 2, 3)
// ys == List(1, 2, 3, 4)

import scala.collection.mutable
val buf = mutable.ListBuffer(1, 2, 3)
buf += 4                       // mutates buf in place
// buf == ListBuffer(1, 2, 3, 4)
```

Idiomatic Scala uses **immutable** collections by default, even in production code. Reasons:

- Thread safety: nobody can mutate them out from under you.
- Sharing: passing a `List` around is free (no defensive copies).
- Reasoning: `xs.size` will be the same value at every line of your function.

Modern immutable collections share structure under the hood — adding an element to a million-element `Vector` is O(log n), not O(n). You're not paying the obvious cost.

Use mutable collections when:
- You're in a hot loop and profiling shows allocation is a bottleneck.
- You're building a result and converting to immutable at the end (`mutable.ListBuffer().toList`).
- You're interoperating with Java code that expects mutation.

---

## 3. `List`

Singly-linked, immutable, optimized for `head`/`tail`/prepend. The most-used Scala collection.

```scala
val xs = List(1, 2, 3, 4)
val ys = 0 :: xs              // List(0, 1, 2, 3, 4)  — prepend (O(1))
val zs = xs ::: List(5, 6)    // List(1, 2, 3, 4, 5, 6) — concat (O(n))

xs.head           // 1   — O(1)
xs.tail           // List(2, 3, 4)  — O(1)
xs.last           // 4   — O(n)
xs(2)             // 3   — O(n)  (don't index into Lists in loops!)
xs.length         // 4   — O(n)  (yep, also O(n))
xs.isEmpty        // false
Nil               // empty List
```

`Nil` is the empty `List`. The `::` (cons) operator is right-associative:

```scala
val xs = 1 :: 2 :: 3 :: Nil     // List(1, 2, 3)
//          ^   ^   ^
//          |   |   Nil.::(3) -> List(3)
//          |   List(3).::(2) -> List(2, 3)
//          List(2, 3).::(1)  -> List(1, 2, 3)
```

Pattern matching is where `List` shines:

```scala
def sum(xs: List[Int]): Int = xs match {
  case Nil       => 0
  case h :: rest => h + sum(rest)
}
```

---

## 4. `Vector`

Tree-structured, immutable, **the right default for general use** when you don't specifically need `List`'s pattern-matching shape.

```scala
val v = Vector(1, 2, 3, 4)
v(2)              // 3       — effectively O(1)
v.length          // 4       — O(1)
v :+ 5            // append, O(log n)
0 +: v            // prepend, O(log n)
v.updated(1, 99)  // Vector(1, 99, 3, 4) — O(log n)
```

`Vector` gives you near-O(1) for everything by storing elements in a 32-way tree. It's the immutable equivalent of `ArrayList`.

**Rule of thumb:** if you're not doing recursive `head`/`tail` work, prefer `Vector`.

---

## 5. `Array`

JVM's native `T[]`. Mutable, fixed-size, O(1) random access. Most useful for performance-sensitive code or Java interop.

```scala
val arr = Array(1, 2, 3, 4)
arr(0) = 99            // mutate (Arrays are mutable!)
arr(0)                 // 99

val zeroes = new Array[Int](100)    // length 100, all 0
val empty = Array.empty[Double]
```

`Array` has all the collection methods (via implicit `WrappedArray`/`ArrayOps`), but the result of those methods is usually a different collection type. To preserve the array, use `clone()` or copying methods:

```scala
val a = Array(1, 2, 3)
val b = a.map(_ * 10)             // Array[Int]
val c: Array[Int] = a.filter(_ > 1)
```

---

## 6. `Range`

A lazy sequence of evenly-spaced numbers. Created with `to`, `until`, or `by`:

```scala
1 to 5             // Range(1, 2, 3, 4, 5)
1 until 5          // Range(1, 2, 3, 4)
1 to 10 by 2       // Range(1, 3, 5, 7, 9)
10 to 1 by -1      // Range(10, 9, 8, 7, 6, 5, 4, 3, 2, 1)
```

A `Range` doesn't materialize all the values; it computes them on demand. Perfect for `for` loops:

```scala
for (i <- 1 to 10) println(i)
```

Convert to a `List`/`Vector` if you need to keep it:

```scala
(1 to 5).toList        // List(1, 2, 3, 4, 5)
```

---

## 7. `LazyList`

Like a `List`, but every element is computed on demand and memoized. Useful for infinite sequences.

```scala
val naturals: LazyList[Int] = LazyList.from(1)   // 1, 2, 3, 4, ...

naturals.take(5).toList     // List(1, 2, 3, 4, 5)
naturals.head               // 1

val fibs: LazyList[Int] =
  0 #:: 1 #:: fibs.zip(fibs.tail).map { case (a, b) => a + b }

fibs.take(10).toList        // List(0, 1, 1, 2, 3, 5, 8, 13, 21, 34)
```

`#::` is the lazy cons operator (right-associative, like `::`).

**Watch out for memory:** because `LazyList` memoizes computed elements, holding a reference to the head of an infinite list keeps every computed element alive. If you don't need that, use an `Iterator` (next section) instead.

> **Scala 2 note:** Scala 2.13 renamed `Stream` to `LazyList` because the old `Stream` had subtly different (head-eager) semantics. Old code may use `Stream`.

---

## 8. `Set`

Unordered, unique elements.

```scala
val s = Set(1, 2, 3, 2, 1)      // Set(1, 2, 3)
s.contains(2)                    // true
s + 4                            // Set(1, 2, 3, 4)
s - 1                            // Set(2, 3)
s ++ Set(5, 6)                   // Set(1, 2, 3, 5, 6)
s & Set(2, 3, 4)                 // Set(2, 3)   intersection
s | Set(2, 3, 4)                 // Set(1, 2, 3, 4)  union
s &~ Set(2, 3)                   // Set(1)      diff
```

**Concrete types:**

- `Set[A]` (default) — `HashSet`, O(1) average.
- `TreeSet[A]` — sorted by an `Ordering[A]`, O(log n) ops.
- `BitSet` — for `Set[Int]` of small non-negative ints, very compact.
- `ListSet` — preserves insertion order, but ops are O(n).

---

## 9. `Map`

Key-value pairs.

```scala
val m = Map("a" -> 1, "b" -> 2, "c" -> 3)
m("a")              // 1   — throws if missing
m.get("a")          // Some(1)
m.get("z")          // None
m.getOrElse("z", 0) // 0

m + ("d" -> 4)      // adds a pair
m - "a"             // removes a key
m.keys              // Iterable("a", "b", "c")
m.values            // Iterable(1, 2, 3)
m.contains("a")     // true
m.size              // 3

// iterate
for ((k, v) <- m) println(s"$k -> $v")
m.foreach { case (k, v) => println(s"$k -> $v") }
```

The `key -> value` syntax is sugar for `(key, value)` — `->` is a method on any type that returns a tuple.

**Concrete types:**

- `Map[K, V]` (default) — `HashMap`, O(1) average.
- `TreeMap[K, V]` — sorted by an `Ordering[K]`.
- `ListMap[K, V]` — insertion-ordered, O(n) ops.
- `LinkedHashMap` — mutable, insertion-ordered.

---

## 10. Tuples

A short, fixed-arity heterogeneous container. Up to 22 elements in Scala 2 (lifted in Scala 3).

```scala
val pair = (1, "hello")          // Tuple2[Int, String]
pair._1                          // 1
pair._2                          // "hello"

val (a, b) = pair                // destructure

val triple = (1, "x", true)
triple._3                        // true
```

Useful as ad-hoc structs. For anything you'd reach for repeatedly, define a `case class`:

```scala
case class Person(name: String, age: Int)   // better than (String, Int)
```

> **Scala 3 note:** Scala 3 has *named tuples*:
> ```scala
> // scala 3
> val person = (name = "Bhaskar", age = 30)
> person.name   // "Bhaskar"
> ```

---

## 11. Common operations

Most of these work on every `Iterable`. Examples use `List`, but apply broadly.

### Transforming

```scala
val xs = List(1, 2, 3, 4)

xs.map(_ * 2)                 // List(2, 4, 6, 8)
xs.flatMap(x => List(x, x))   // List(1, 1, 2, 2, 3, 3, 4, 4)
xs.collect { case x if x > 2 => x * 10 }   // List(30, 40)  — partial map
xs.zip(List("a", "b", "c"))   // List((1,a), (2,b), (3,c))
xs.zipWithIndex                // List((1,0), (2,1), (3,2), (4,3))
```

### Filtering and partitioning

```scala
xs.filter(_ % 2 == 0)         // List(2, 4)
xs.filterNot(_ % 2 == 0)      // List(1, 3)
xs.partition(_ % 2 == 0)      // (List(2,4), List(1,3))
xs.takeWhile(_ < 3)           // List(1, 2)
xs.dropWhile(_ < 3)           // List(3, 4)
xs.span(_ < 3)                // (List(1,2), List(3,4))
xs.distinct                   // remove duplicates
```

### Reducing

```scala
xs.sum                                    // 10
xs.product                                // 24
xs.min                                    // 1
xs.max                                    // 4
xs.foldLeft(0)(_ + _)                     // 10
xs.foldRight(0)(_ + _)                    // 10
xs.reduce(_ + _)                          // 10  — empty list throws
xs.reduceOption(_ + _)                    // Some(10)
xs.count(_ > 2)                           // 2
xs.forall(_ > 0)                          // true
xs.exists(_ > 3)                          // true
xs.find(_ > 2)                            // Some(3)
```

### Grouping

```scala
val words = List("apple", "ant", "bear", "berry", "cat")

words.groupBy(_.head)
// Map(a -> List(apple, ant), b -> List(bear, berry), c -> List(cat))

words.groupBy(_.head).view.mapValues(_.size).toMap
// Map(a -> 2, b -> 2, c -> 1)

words.groupMapReduce(_.head)(_.length)(_ + _)
// Map(a -> 8, b -> 9, c -> 3)
```

`groupMapReduce` is one of the under-appreciated power tools — group, transform, reduce in one pass.

### Sorting

```scala
List(3, 1, 4, 1, 5, 9, 2, 6).sorted              // ascending
List(3, 1, 4).sorted(Ordering.Int.reverse)       // descending
List("banana", "apple", "cherry").sorted

case class P(name: String, age: Int)
val ps = List(P("Bhaskar", 30), P("Alice", 25))
ps.sortBy(_.age)            // by age
ps.sortBy(p => (p.age, p.name))   // composite
ps.sortWith(_.age < _.age)
```

---

## 12. `view` for laziness

By default, collection ops are *strict* — each call materializes a new collection:

```scala
val xs = (1 to 1_000_000).toList
xs.map(_ * 2).filter(_ > 1000).take(5)
// allocates a List of 1 million doubles, then a filtered List, then a List of 5
```

A `view` makes operations lazy until you ask for a result:

```scala
xs.view.map(_ * 2).filter(_ > 1000).take(5).toList
// only computes 5 elements through the pipeline; no intermediate collections
```

`view` is your friend for long pipelines on large collections. Don't use `view` for short pipelines on small collections — the overhead isn't worth it.

---

## 13. `Iterator`

A one-shot, mutable cursor. Doesn't memoize; once you've traversed it, it's exhausted.

```scala
val it = List(1, 2, 3).iterator
it.next()      // 1
it.next()      // 2
it.hasNext     // true
it.next()      // 3
it.hasNext     // false
```

Useful for:

- Streaming through a huge file/source without loading it all into memory.
- One-time pipelines where `view` is overkill.

```scala
val source = scala.io.Source.fromFile("big.csv")
try {
  source.getLines()
    .map(_.split(","))
    .filter(_.length == 5)
    .foreach(processRow)
} finally source.close()
```

---

## 14. Mutable collections

Importable from `scala.collection.mutable`. Useful in narrow cases:

```scala
import scala.collection.mutable

val buf = mutable.ListBuffer.empty[Int]
buf += 1; buf += 2; buf += 3
buf.toList                     // immutable List(1, 2, 3)

val arr = mutable.ArrayBuffer(1, 2, 3)
arr += 4
arr.remove(0)
arr                            // ArrayBuffer(2, 3, 4)

val mmap = mutable.Map.empty[String, Int]
mmap("a") = 1
mmap += ("b" -> 2)
mmap("c") = mmap.getOrElse("c", 0) + 1
```

Common mutable types: `ListBuffer`, `ArrayBuffer`, `Queue`, `Stack`, `HashSet`, `HashMap`, `LinkedHashMap`, `PriorityQueue`.

The discipline: **build mutably inside a function, return immutably**. The mutation is hidden from the caller.

---

## 15. Choosing a collection

A short, opinionated guide:

| Need | Use |
|---|---|
| Default sequence | `Vector` |
| Recursion / pattern matching on head/tail | `List` |
| Random access | `Vector` (immutable) or `Array` (mutable) |
| Building incrementally | `ListBuffer` or `ArrayBuffer`, then `.toList` / `.toVector` |
| Unique values | `Set` |
| Sorted unique values | `TreeSet` |
| Key-value | `Map` |
| Sorted key-value | `TreeMap` |
| Insertion-ordered map | `LinkedHashMap` (mutable) |
| Infinite/lazy sequence | `LazyList` (memoizing) or `Iterator` (one-shot) |
| Large pipeline transformations | `.view.map.filter.take.toList` |
| Heterogeneous fixed group | `Tuple` (small) or `case class` |

When in doubt: `Vector` for sequences, `Map` for key-value, `Set` for unique. You'll rarely go wrong.

---

## What you should now know

- The collection hierarchy and where each concrete type fits.
- Why immutable is the default and when mutable is appropriate.
- The cost characteristics of `List` vs `Vector` vs `Array` vs `LazyList`.
- The full menu of `map`/`flatMap`/`filter`/`fold`/`reduce`/`groupBy` and friends.
- When to use `view` and when to use `Iterator`.
- A heuristic for picking a collection without overthinking it.

The next chapter ([Chapter 5 — Pattern Matching](05-pattern-matching.md)) is one of Scala's signature features and absolutely central to working with collections (you've already seen `case h :: rest =>`).

---

[← Previous: Chapter 3 — Strings](03-strings-and-interpolation.md) | [Back to README](README.md) | [Next: Chapter 5 — Pattern Matching →](05-pattern-matching.md)
