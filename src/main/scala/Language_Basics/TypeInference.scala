package Language_Basics

object TypeInference extends App {

  val n = 42                    // Int
  val x = 3.14                  // Double
  val name = "Bhaskar"          // String
  val flag = true               // Boolean
  val list = List(1, 2, 3)      // List[Int]
  val tuple  = (1, "a", true)    // (Int, String, Boolean)

  //Inference also works for return types of methods:

  def double(n: Int) = n * 2          // return type inferred: Int
  def double2(n: Int): Int = n * 2     // explicit; same thing

  //Style guide: infer locals freely, annotate public method/value signatures explicitly. The signature is part of the contract.

  // good
  def parseAge(input: String): Option[Int] = input.toIntOption

  // works but the contract is now hidden
  def parseAge2(input: String) = input.toIntOption

  //What Scala can't infer
  //Recursive method return types — must be annotated.
  //Sometimes the "most general" type isn't what you want; annotate to widen.

  // won't compile — can't infer return type for recursive method
  // def fact(n: Int) = if (n == 0) 1 else n * fact(n - 1)

  def fact(n: Int): Int = if (n == 0) 1 else n * fact(n - 1)   // OK
  


}
