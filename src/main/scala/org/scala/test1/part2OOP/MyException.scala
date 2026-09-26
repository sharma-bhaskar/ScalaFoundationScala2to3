package org.scala.test1.part2OOP

import scala.annotation.tailrec

object MyException {

  //Exception = specicial incident when to crash our application

  val aString: String = null

//  val aWeirdValue: Nothing = throw new NullPointerException

  //type throwable
  //error eg SOError,OOMError

  //and Logical exception error
  // throw is the type of Nothing

  def getInt(withException: Boolean): Int = if (withException) throw new RuntimeException("No int for you") else 43

  val potentialFail: Int = try {
    getInt(true)
  } catch {
    case e: RuntimeException => 54
    case e: NullPointerException => 56
  } finally {
    //no matter what if will run
    println("I am finally block ")
  }

  //custom execption declaration

  class MyException extends RuntimeException {
    override def getMessage: String = "MY Exception"
  }

  val myException = new MyException


  /**
   * Exercises:
   *
   * 1. Crash with SOError
   * 2. Crash with OOMError
   * 3. Find an element matching a predicate in LList
   */

  def SOError(): Int = {
    def infinite():Int = 1 + infinite()
    infinite()
  }

  def oomCrash(): Unit = {
    @tailrec
    def bigString(n: Int, acc: String): String =
      if (n == 0) acc
      else bigString(n - 1, acc + acc)

    bigString(56175363, "Scala")
  }


  def main(args: Array[String]): Unit = {
    println(potentialFail)
//    SOError()
    oomCrash()
    val throwMYExecption = throw new MyException
  }
}
