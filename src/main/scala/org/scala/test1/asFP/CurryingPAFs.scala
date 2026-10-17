package org.scala.test1.asFP

object CurryingPAFs {

  // currying - is the capabilities of passing value one at a time

  val supperAdder: Int => Int => Int = x => y => x + y

  val add3 = supperAdder(3)
  val eight = add3(5)

  val eight_v2 = supperAdder(3)(5) //this is also available for curried method

  //curried method

  def curriedMethod(x: Int)(y: Int): Int = x + y

  //method != function value
  //method is part of JVM, scala allow fast coversion to method to function using ETA expansion

  // ETA expansion is internal process to covert method to function

  val add4 = curriedMethod(4)

  val nine = add4(5)

  //When method invo few argument list and method

  def inc(x: Int): Int = x + 1

  val aList = List(1, 2, 3)
  val incList = aList.map(inc) //in this we are passing inc but this is only possible using eta expansion

  def contcatnetor(a: String, b: String, c: String): String = a + b + c

  //now we can use this like this _:String where underscore wait for value when it will  pass at runtime

  val insertName = contcatnetor("Hello ", _: String, ",another String")

  val greeting = insertName("Bhaskar")

  /**
   * Currying using mathmatics
   *
   * add(x, y) = x + y
   * it take two argument (Int,Int) => Int and return Int => (Int,Int)
   * also PartialFunction is also same where we suppose we already have this curried func but passing one by one
   *
   * so hierarchy is like
   * Currrying
   * |
   * Function return fun
   * |
   * Partial applied func
   * |
   * provided first arg
   * |
   * Closure
   * |
   * reference function
   *
   * */

  val addXY: Int => Int => Int = x => y => x + y

  val adderRes = addXY(10)(20) //curried but this gives 30
  // but if we gives only
  val xy = addXY(10) // this will function and we can save the result for further


  /**
   * Exercises
   */

  val simpleAddFunction = (x: Int, y: Int) => x + y

  def simpleAddMethod(x: Int, y: Int) = x + y

  def curriedMethods(x: Int)(y: Int) = x + y

  // 1 - obtain an add7 function: x => x + 7 out of these 3 definitions

  val add7 = (x: Int) => simpleAddFunction(x, 7)
  val add7_v2 = (x: Int) => simpleAddMethod(x, 7)
  val add7_v3 = (x: Int) => curriedMethod(7)(x)
  val add7_v4 = curriedMethod(7)
  val add7_v5 = simpleAddMethod(7, _)
  val add7_v6 = simpleAddMethod(_, 7)
  val add7_v7 = simpleAddFunction.curried(7)

  // 2 - process a list of numbers and return their string representations under different formats
  // step 1: create a curried formatting method with a formatting string and a value

  def curriedFormatter(fmt: String)(number: Double): String = fmt.format(number)

  // step 2: process a list of numbers with various formats
  val piWith2Dec = "%8.6f".format(Math.PI) // 3.14
  val someDecimals = List(Math.PI, Math.E, 1, 9.8, 1.3e-12)

  // methods vs functions + by-name vs 0-lambdas
  def byName(n: => Int) = n + 1

  def byLambda(f: () => Int) = f() + 1

  def method: Int = 42

  def parenMethod(): Int = 42

  byName(23) // ok

  def main(args: Array[String]): Unit = {
    println(greeting)

  }
}
