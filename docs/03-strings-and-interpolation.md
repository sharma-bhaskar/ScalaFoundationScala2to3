# Chapter 3 — Strings and String Interpolation

> Scala's `String` is `java.lang.String`. The interesting things are *interpolation* (the `s""`, `f""`, `raw""` prefixes) and the ability to define your own.

**In this chapter:**
1. [String literals](#1-string-literals)
2. [Multi-line strings](#2-multi-line-strings)
3. [The `s` interpolator](#3-the-s-interpolator)
4. [The `f` interpolator (printf-style)](#4-the-f-interpolator-printf-style)
5. [The `raw` interpolator](#5-the-raw-interpolator)
6. [Custom interpolators](#6-custom-interpolators)
7. [Common String API methods](#7-common-string-api-methods)
8. [`StringBuilder` for performance](#8-stringbuilder-for-performance)

---

## 1. String literals

```scala
val s = "hello"
val withQuote = "she said \"hi\""        // escape with backslash
val withTab = "col1\tcol2\tcol3"
val withNewline = "line 1\nline 2"
val withUnicode = "café"            // "café"
```

Standard escape sequences: `\n`, `\t`, `\\`, `\"`, `\'`, `A` (Unicode).

A `String` is immutable — every "modifying" operation returns a new `String`:

```scala
val s = "hello"
val upper = s.toUpperCase    // "HELLO"
// s is still "hello"
```

---

## 2. Multi-line strings

Triple quotes `"""..."""` create a literal that ignores escape sequences and preserves newlines:

```scala
val xml = """<root>
              <item>1</item>
              <item>2</item>
            </root>"""
```

The leading whitespace on each line is part of the string. To strip it, use `stripMargin` with a margin character (default `|`):

```scala
val sql = """SELECT id, name
            |FROM users
            |WHERE active = true""".stripMargin

// SELECT id, name
// FROM users
// WHERE active = true
```

`stripMargin` removes whitespace + the `|` character, line by line.

---

## 3. The `s` interpolator

The most common one. Prefix a string literal with `s` to enable `$variable` and `${expression}` substitutions:

```scala
val name = "Bhaskar"
val greeting = s"hello, $name"                  // "hello, Bhaskar"
val math = s"1 + 1 = ${1 + 1}"                  // "1 + 1 = 2"
val complex = s"first item: ${List(1,2,3).head}"  // "first item: 1"
```

`$name` is sugar for `${name}`. Use the braced form whenever the variable name has anything funky:

```scala
val n = 5
s"$n items"        // "5 items"
s"${n}items"       // "5items"  — braces required, otherwise tries to find $nitems
```

Multi-line and interpolation combine:

```scala
val user = "Bhaskar"
val items = List(1, 2, 3)
val report =
  s"""Report for $user
     |Item count: ${items.size}
     |Total: ${items.sum}""".stripMargin
```

---

## 4. The `f` interpolator (printf-style)

Like `s`, but each substitution can have a `printf`-style format specifier:

```scala
val pi = 3.14159
f"$pi%1.2f"               // "3.14"
f"$pi%10.4f"              // "    3.1416"   (10-char width, 4 decimals)
f"${"hi"}%-10s|"          // "hi        |"   (left-align, 10 chars)
f"${42}%05d"              // "00042"          (zero-pad to 5)
f"${255}%X"               // "FF"             (uppercase hex)
```

Format specifiers come from `java.util.Formatter`. The compiler also type-checks them:

```scala
f"${3.14}%d"
// error: type mismatch; expected: Int, actual: Double
```

That compile-time check is one of the nicest things about `f` over a runtime `String.format`.

---

## 5. The `raw` interpolator

Like `s`, but ignores escape sequences. Good for regexes and Windows paths:

```scala
val regex = raw"\d+\.\d+"          // "\d+\.\d+"  literal backslashes
val winPath = raw"C:\Users\me"     // "C:\Users\me"

// without raw:
val regex2 = "\\d+\\.\\d+"         // same string, but harder to read
```

`$variable` interpolation still works inside `raw""`.

---

## 6. Custom interpolators

You can define your own. Any prefix `xxx""` is desugared to a method `xxx` on a value imported from `StringContext`:

```scala
implicit class JsonHelper(val sc: StringContext) extends AnyVal {
  def json(args: Any*): String = {
    val parts = sc.parts.iterator
    val argIter = args.iterator
    val sb = new StringBuilder(parts.next())
    while (argIter.hasNext) {
      sb.append('"').append(argIter.next()).append('"')
      sb.append(parts.next())
    }
    sb.toString
  }
}

val name = "Bhaskar"
val age = 30
val js = json"""{"name": $name, "age": $age}"""
// {"name": "Bhaskar", "age": "30"}
```

How it works: `json"..."` desugars to `new StringContext(parts*).json(args*)`. The `parts` are the string fragments between substitutions; `args` are the substituted values.

Use cases: SQL safety (auto-escaping), JSON building, custom DSLs. Most production projects don't roll their own — but seeing it once removes the magic.

> **Scala 3 note:** Custom interpolators in Scala 3 use `extension` methods on `StringContext` instead of an `implicit class`:
> ```scala
> // scala 3
> extension (sc: StringContext) def json(args: Any*): String = ???
> ```

---

## 7. Common String API methods

Inherited from `java.lang.String` plus extras from `StringOps` (a Scala wrapper).

### Inspection

```scala
val s = "Hello, World"
s.length              // 12
s.isEmpty             // false
s.nonEmpty            // true
s.startsWith("Hel")   // true
s.endsWith("rld")     // true
s.contains("World")   // true
s.indexOf("o")        // 4
s.lastIndexOf("o")    // 8
s(0)                  // 'H'  — Char
s.charAt(0)           // 'H'
```

### Slicing

```scala
s.substring(7)        // "World"
s.substring(0, 5)     // "Hello"
s.take(5)             // "Hello"     (Scala collection-style)
s.drop(7)             // "World"
s.takeRight(5)        // "World"
s.dropRight(7)        // "Hello"
s.slice(7, 12)        // "World"     (inclusive start, exclusive end)
```

### Transformation

```scala
s.toUpperCase         // "HELLO, WORLD"
s.toLowerCase         // "hello, world"
s.trim                // strips whitespace at both ends
s.strip               // similar; strips Unicode whitespace (Java 11+)
s.reverse             // "dlroW ,olleH"
s.replace(",", ";")   // "Hello; World"
s.replaceAll("[aeiou]", "*")   // regex replace
```

### Splitting and joining

```scala
val csv = "a,b,c"
csv.split(",").toList                // List("a", "b", "c")
csv.split(",", -1).toList            // includes trailing empties

List("a", "b", "c").mkString(",")    // "a,b,c"
List("a", "b", "c").mkString("[", ", ", "]")   // "[a, b, c]"
```

### Comparison

```scala
"abc" == "abc"                  // true (Scala's == calls equals)
"abc".equalsIgnoreCase("ABC")   // true
"abc".compareTo("abd")          // < 0  (alphabetic ordering)
```

In Scala, `==` is *value equality* (calls `equals`), unlike Java where `==` on Strings is reference equality and you need `.equals()`.

### Conversion

```scala
"42".toInt                  // 42  — throws on bad input
"42".toIntOption            // Some(42) — safe
"3.14".toDouble             // 3.14
"true".toBoolean            // true

42.toString                 // "42"
List(1, 2, 3).toString      // "List(1, 2, 3)"
```

### Iteration (a String is a `Seq[Char]`)

```scala
"abc".map(_.toUpper)             // "ABC"
"abc".foreach(println)           // a, b, c
"abc".filter(_ != 'b')           // "ac"
"abracadabra".count(_ == 'a')    // 5
"abc".zipWithIndex                // Vector((a,0), (b,1), (c,2))
```

This is one of Scala's quiet superpowers — every collection method works on strings because of `StringOps`.

---

## 8. `StringBuilder` for performance

For one-off concatenation, `+` and interpolation are fine. For building strings in a loop, use `StringBuilder`:

```scala
val sb = new StringBuilder
for (i <- 1 to 1000) sb.append(s"item-$i\n")
val result = sb.toString
```

Why? `+` on `String` allocates a new immutable string each time. The above loop with `+` would allocate 1,000 increasingly-large strings. `StringBuilder` mutates an internal buffer.

Modern JVMs sometimes optimize chains of `+` into `StringBuilder` automatically (the `invokedynamic`-based `makeConcatWithConstants`), but that only kicks in for *expressions*, not loops. Loops still need explicit `StringBuilder`.

For modest sizes, don't bother — readability wins. For tight loops in a hot path, `StringBuilder` is the right choice.

---

## What you should now know

- All four built-in interpolators: `s` (general), `f` (printf), `raw` (no escapes).
- `stripMargin` for cleaning up multi-line strings.
- How to define your own interpolator via `StringContext`.
- The most useful methods on `String` / `StringOps`, from inspection to slicing to iteration.
- When to reach for `StringBuilder`.

The next chapter ([Chapter 4 — Collections](04-collections.md)) is the longest one in the guide — Scala's collection library is enormous and central to everything else.

---

[← Previous: Chapter 2 — Functions and Methods](02-functions-and-methods.md) | [Back to README](README.md) | [Next: Chapter 4 — Collections →](04-collections.md)
