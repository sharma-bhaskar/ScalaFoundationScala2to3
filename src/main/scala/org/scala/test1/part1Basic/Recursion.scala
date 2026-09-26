package org.scala.test1.part1Basic

import scala.annotation.tailrec

object Recursion {


  // recursive is repition = "recursive"

  // sum of n number
  def sumUtil(n: Int): Int = {
    if (n <= 0) 0
    else n + sumUtil(n - 1)
  }

  def sumUtil_v2(n: Int): Int = {
    @tailrec
    def sumUtilTail(x: Int, acc: Int): Int = {
      if (x <= 0) acc
      else sumUtilTail(x - 1, acc + x)
    }

    sumUtilTail(n, 0)
  }

  //sum of no btw a & b

  def sumNoBtw(a: Int, b: Int): Int = {
    if (a > b) 0
    else a + sumNoBtw(a + 1, b)
  }

  //tail rec way

  def sumNoBTwTail(a: Int, b: Int): Int = {
    @tailrec
    def helperTail(currtNo: Int, acc: Int): Int = {
      if (currtNo > b) acc
      else helperTail(currtNo + 1, acc + currtNo)
    }

    helperTail(a, 0)
  }


  /**
   * Exercises
   * 1. Concatenate a string n times
   * 2. Fibonacci function, tail recursive
   * 3. Is isPrime function tail recursive or not?
   */

  def concatentionTail(st: String, n: Int): String = {
    @tailrec
    def tailString(x: Int, acc: String): String = {
      if (x <= 0) acc
      else tailString(x - 1, acc + st)
    }

    tailString(n, "")
  }

  def tailFib(n: Int): Int = {
    @tailrec
    def helper(x: Int, a: Int, b: Int): Int = {
      if (x == 0) a
      else helper(x - 1, b, a + b)
    }

    helper(n, 0, 1)
  }

  def main(args: Array[String]): Unit = {
    println(concatentionTail("Scala", 3))
    println(tailFib(5))
    println(tailFib(6))
    println(tailFib(7))
  }

}
