package org.scala.test1.part1Basic

object CBVvsCBN {

  //Call by value = argument evaluted before function invocation

  def aFunc(arg: Int) = arg + 1

  val aComm = aFunc(45 + 45) // so in this 46 + 45 please pre calculated and thn it will pass it to function to call so 90 will pass

  // now call by name is to pass like an expression

  def aByName(arg: => Int): Int = arg + 1

  val aName = aByName(45 + 45) // in this case value is passing as it is like 45 + 45 and so 45 + 45 + 1 now it will calculate

  // so call by name is delayed expression to evalute the value rather than precompute the value

  // another example

  def printTwiceByValue(x: Long): Unit = {
    println("By Value: " + x)
    println("By Value: " + x)
  }

  def printTwiceByName(x: Long): Unit = {
    println("By Value: " + x)
    println("By Value: " + x)
  }


  def main(args: Array[String]): Unit = {
    printTwiceByValue(System.nanoTime())
    printTwiceByName(System.nanoTime()) //both different result so in this same we will get delayed evaluation
  }
}
