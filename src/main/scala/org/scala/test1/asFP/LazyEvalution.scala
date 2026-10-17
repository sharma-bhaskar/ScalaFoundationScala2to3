package org.scala.test1.asFP

object LazyEvalution {

  //when I call this is expression this will evalulate everytime when I call this expression
  // so deal with this problem
  val x = {
    println("Hello")
    3
  }

  //scala support lazy evaluation

  lazy val y = {
    println("Hello")
    3
  }

  // scala has kyeword c/d lazy which we can attach to the value and the modifiers the delay the evaluate of modification
  // lazy delay the evaluation of a value until the first use'

  //so lazy evalution is call by need = call by name + lazy keyword

  //call by name
  def inc(x: => Int) = {
    x + x + x + 1
  }

  def needByMethod(n: => Int): Int = {
    lazy val lazyN = n
    lazyN + lazyN + lazyN + 1
  }
  //in this case pass & this is combination of delayed this opr called memoization

  /**
   * example 2 withFilter
   * */

  def lessThan30(n: Int): Boolean = {
    println(s"$n is less than 30")
    n < 30
  }

  def lessThan20(n: Int): Boolean = {
    println(s"$n is less than 30")
    n < 20
  }

  val numbers = List(1, 2, 3, 4, 5, 7)

  def demoFilter() = {
    val lt30 = numbers.filter(lessThan30)
    val result = lt30.filter(lessThan20)
    println(result)
  }

  // now do the same thing with

  def demoWithFilter() = {
    val lt30 = numbers.withFilter(lessThan30)
    val result = lt30.withFilter(lessThan20)
    println(result.map(identity))
  }

  def main(args: Array[String]): Unit = {
    println(x) // in this case x need to evaluate
    println(y) // in this case y is bound to compute and print 3 and
    println(y)
//    println(demoFilter())
    println("Demo with filter " + demoWithFilter())

  }

}
