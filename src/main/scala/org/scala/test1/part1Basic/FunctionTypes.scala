package org.scala.test1.part1Basic

import scala.annotation.tailrec

object FunctionTypes {

  //function -- reusable piece of code that you can invoke with some argument and return results

  //concatenation of a function using simple imperative ways
  def aFunction(a: String, b: Int): String = a + " " + b

  //invocation of function like this
  val aFunctionInvocation = aFunction("Scala", 3)

  //other declaration type of function

  def noArgFunc(): Int = 45

  def aParameterFunction: Int = 45

  //v imp recursive function where define the function in recursive ways

  //String concatenation using recursive
  def stringConcatenation(str: String, n: Int): String = {
    if (n == 0) ""
    else if (n == 1) str
    else str + stringConcatenation(str, n - 1)
  }

  //pattern matching recursion

  def stringConcatenationMatch(str: String, n: Int): String = n match {
    case n if n <= 0 => ""
    case 1 => str
    case _ => str + stringConcatenationMatch(str, n - 1)
  }


  //now invocation of a function
  val scalax3: String = stringConcatenationMatch("Scala", 3)

  // in scala they say when you need loop use recursion

  //void function

  def voidFunction(aString: String): Unit = println(aString)

  def computeFunctionStringWithSideEffects(aString: String): String = {
    voidFunction(aString) // this is creating a side effects into the systems
    // in functional prgramming world people generally avoid side effect as its create more unstructure or unassign code
    aString + aString
  }

  // now define a bigFunction

  def aBigFunction(n: Int): Int = {
    def aSmallerFunction(a: Int, b: Int) = a + b

    aSmallerFunction(n, n + 1)
  }

  /**
   * Exercises
   * 1. A greeting function (name, age) => "Hi my name is $name and I am $age years old."
   * 2. Factorial function n => 1 * 2 * 3 * .. * n
   * 3. Fibonacci function
   *    fib(1) = 1
   *    fib(2) = 1
   *    fib(3) = 1 + 1
   *    fib(n) = fib(n-1) + fib(n-2)
   *
   * 4. Tests if a number is prime
   */

  def isGreeting(name: String, age: Int): String = s"Hi my name is $name and I am $age years old"

  // simple recursive approach
  def isFactorial(n: Int): Int = {
    if (n == 0 || n == 1) 1
    else n * isFactorial(n - 1)
  }

  // tail recursive way
  def isFactorialTail(n: Int): Int = {
    @tailrec
    def helperFactorial(x: Int, acc: Int): Int = {
      if (x == 0 || x == 1) acc
      else helperFactorial(x - 1, acc * x)
    }

    helperFactorial(n, 1)
  }

  //fibonacci series recursive

  def fib(n: Int): Int = n match {
    case 0 | 1 => n
    case _ => fib(n - 1) + fib(n - 2)
  }

  //tail recursive
  def tailFib(n: Int): Int = {
    @tailrec
    def tailHelper(x: Int, a: Int, b: Int): Int = {
      x match {
        case 0 => a
        case _ => tailHelper(x - 1, b, a + b)
      }
    }

    tailHelper(n, 0, 1)
  }

  def isPrime(n: Int): Boolean = {
    @tailrec
    def isPrimeUtil(x: Int, d: Int): Boolean = {
      if (n < 2) false
      else if (x % d == 0) false
      else if (d * d > x) true
      else isPrimeUtil(x, d + 1)
    }

    isPrimeUtil(n, 2)
  }

  //another prime tail

  def isPrimeCheck(n: Int): Boolean = {
    @tailrec
    def isPrimeUtil(t: Int): Boolean = {
      if (t <= 1) true
      else n % t == 0 && isPrimeUtil(t - 1)
    }

    isPrimeUtil(n / 2)
  }

  def main(args: Array[String]): Unit = {
    println(scalax3)
    println(isGreeting("Daniel", 9))
    println(isFactorialTail(5))
    println(tailFib(5))
    println(isPrimeCheck(16))

  }

}
