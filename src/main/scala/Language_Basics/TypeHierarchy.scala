package Language_Basics

//Every type in Scala descends from Any. There is no "primitive vs object" split as there is in Java — at the language level, everything is an object.
/**
 *                         Any
                       /   \
                  AnyVal    AnyRef  (= java.lang.Object)
                  /  | \      |  \
              Int Double Boolean  String, List, your classes...
                  ...
                       \   /
                       Null  (subtype of every AnyRef)
                         |
                     Nothing  (subtype of every type)
 *
 *
 * */
object TypeHierarchy extends App {
  //Any is the root. Has ==, !=, equals, hashCode, toString, ##.

  //AnyVal is the parent of value types — Int, Long, Double, Float, Char, Byte, Short, Boolean, Unit.
  // They map directly to JVM primitives when possible (no boxing) but appear as objects to your code.

  //AnyRef is everything else — equivalent to java.lang.Object. All your classes, the standard collections, String, etc.

  //Null is the type of null. It is a subtype of every AnyRef, which is what makes val s: String = null compile

  //Nothing is a subtype of every type. It has no values. Useful as the result type of expressions that don't return — like exceptions:

  def fail(msg: String): Nothing = throw new RuntimeException(msg)

  val x: Int = if (1<2) 42 else fail("nope")   // works because Nothing <: Int

  val empty = List()   // List[Nothing]
  val withInts: List[Int] = empty   // OK

  //Scala 3 note: Scala 3 introduces explicit nulls (opt-in via -Yexplicit-nulls), where Null is no longer a subtype of AnyRef.
  // This makes val s: String = null a compile error and matches modern best practice (Kotlin/Swift-style).

  //Closure

  def add(n:Int) : Int => Int = x =>  x + n

  //Currying
  def add2(a:Int)(b:Int) = a + b

  //Partial Function

  def partialFunction(level:String,msg:String) = s"$level -- $msg"

  val info = partialFunction("INFO",_ : String)
  val warn = partialFunction("WARN", _:String)

  info("TEST")


}
