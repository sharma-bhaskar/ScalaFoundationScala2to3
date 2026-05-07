package Language_Basics

object LanguageBasics extends App{

  val pi = 3.14158 // immutable : cannot be reassigned
  var counter = 0 // mutable: can be reassigned

  counter = counter + 1  // OK

  // pi = 3.0 // compile error

  // val is the default. Reach for var only when you actually need mutation;
  // idiomatic Scala leans on val and creates new values rather than modifying old ones.

  val name: String = "Bhaskar" // type annotation explicit
  val age =  23  // type inferred as Int

  //Scala 3 note: Same syntax. Scala 3 also adds inline val for compile-time constants used in metaprogramming.

  //lazy val Notice "computing..." prints once. Lazy vals are thread-safe by default, Scala uses double-checked locking under the hood
  // lazy val is executed once and then never again.

  lazy val expensive = {
    println("computing...")
    (1 to 1_000_000).sum
  }

  println("before access")
  println(expensive)
  println(expensive)

}
