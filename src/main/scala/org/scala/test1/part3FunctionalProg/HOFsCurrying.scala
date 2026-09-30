package org.scala.test1.part3FunctionalProg

import Language_Basics.TypeHierarchy.x

import scala.annotation.tailrec

object HOFsCurrying {

  //function is first class citizen
  // HOF is take arguments as a function and return the result or
  // take parameter as a value and return the result as a function

  // function as input and return the parameter as result

  val aHof: (Int, Int => Int) => Int = (x, f) => x + 1
  //return the result as a function
  val aAnotherHof: Int => Int => Int = x => y => y + 2 * x


  //  So HOF is basically give me an A, I produce a B.
  //                    f:A -->  B

  /**
   * For example:
   *
   * double(x)=2x
   *
   * or:
   *
   * double : Z --> Z
   *
   * So:
   *
   * double(5)=10
   *
   * */

  //A function that takes another function as input, or returns another function as output.
  /**
   * two shapes of HOf
   * 1. H: (A->B)-> C  Give me a function and A & B and return C
   *    suppose apply
   *    applyTo10(f) => f(10)
   *    now
   *    double(x) = 2x
   *    applyTo10(double) = 20
   * */

  val applyTo10: (Int => Int) => Int = f => f(10)

  val double: Int => Int = x => x * 2

  /**
   * Second shapes H: A=> (B=>C)
   * eg add(x) = > x + y
   * let x = 10
   * then
   * add(10) => 10 + y
   * result => y => 10 + y
   * this is returning a function
   * */

  //eg
  val addX: Int => (Int => Int) = x => y => x + y

  //quick exercise
  val superfunction: (Int, (String, (Int => Boolean)) => Int) => (Int => Int) = (x, f) => y => x + y

  //examples: map, flatmap and filter are all higher order function because they take another function as an argument

  //more example

  //nTimes of a function
  @tailrec
  def nTimes(f: Int => Int, x: Int, n: Int): Int = {
    if (n <= 0) x
    else nTimes(f, n - 1, f(x))
  }

  val plusOne: Int => Int = x => x + 1

  //other way of the same programing using pure function call

  def nTimes_v2(f: Int => Int, n: Int): Int => Int =
    if (n <= 0) (x: Int) => x
    else (x: Int) => nTimes_v2(f, n - 1)(f(x))

  val tenThousand: Int => Int = nTimes_v2(plusOne, 100)
  val oneHunderd: Int = tenThousand(10)


  //Currying
  //Currying is different
  //This is where people often mix HOF and currying. Suppose we have a normal mathematical function:
  //
  //add(x,y)=x+y
  // this expression taking two arguments (INT,INT) => INT  == add(10,20)=30
  // currying transform this idea like this add(10)(20) = 30 so now instead giving two argument at once we can have do one by one

  //What actually happens in currying

  /**
   * so lets add(x)(y) =
   *
   * */
  val supperAdder: Int => Int => Int => Int = (x: Int) => (y: Int) => (z: Int) => x + y + z

  val superAdder_v1: Int => Int => Int = (x: Int) => (y: Int) => x + y

  println(superAdder_v1(10)(20))

  //currying nothing as HOF aas this is returning function

  //eg curriedMethod
  def curriedFormater(fmt: String)(x: Double): String = fmt.format(x)

  val standardFormater: Double => String = curriedFormater("%4.2f")
  val preciseFormater: Double => String = curriedFormater("%10.8f")

  /**
   * 1. LList exercises
   *    - foreach(A => Unit): Unit
   *      [1,2,3].foreach(x => println(x))
   *
   *    - sort((A, A) => Int): LList[A]
   *      [3,2,4,1].sort((x, y) => x - y) = [1,2,3,4]
   *      (hint: use insertion sort)
   *
   *    - zipWith[B](LList[A], (A, A) => B): LList[B]
   *      [1,2,3].zipWith([4,5,6], x * y) => [1 * 4, 2 * 5, 3 * 6] = [4, 10, 18]
   *
   *    - foldLeft[B](start: B)((A, B) => B): B
   *      [1,2,3,4].foldLeft[Int](0)(x + y) = 10
   *      0 + 1 = 1
   *      1 + 2 = 3
   *      3 + 3 = 6
   *      6 + 4 = 10
   *
   *  2. toCurry(f: (Int, Int) => Int): Int => Int => Int
   *     fromCurry(f: (Int => Int => Int)): (Int, Int) => Int
   *
   *  3. compose(f,g) => x => f(g(x)) this take two function and return another function
   *     andThen(f,g) => x => g(f(x))
   */


  // (Int,Int) -> Int == Int -> (Int,Int)
  //2.toCurry(f: (Int, Int) => Int): Int => Int => Int
  val toCurry: ((Int, Int) => Int) => Int => Int => Int = f => x => y => f(x, y)


  //3. fromCurry(f: (Int => Int => Int)): (Int, Int) => Int

  val fromCurry: (Int => Int => Int) => (Int, Int) => Int = f => (x, y) => f(x)(y)

  // 3. compose(f,g) => x => f(g(x)) this take two function and return another function
  val compose: (Int => Int, Int => Int) => Int => Int = (f, g) => x => f(g(x))
  //andThen(f,g) => x => g(f(x))

  val andThen: (Int => Int, Int => Int) => Int => Int = (g, f) => x => g(f(x))

  val incrementer = (x: Int) => x + 1
  val doubler = (x: Int) => 2 * x
  val composedApplication = compose(incrementer, doubler)
  val aSequencedApplication = andThen(incrementer, doubler)


  def main(args: Array[String]): Unit = {

    println(s"Apply HOF function ${applyTo10(double)}")
    println(s"parameter as an value and return function as a result ${addX(10)}") //this will return the function as result
    println(s"parameter as an value and return function as a result ${addX(10)(10)}") // this is total result as its taking two values
    println(s"${nTimes(plusOne, 100, 0)}")

    //currying
    println(s"currying supperAdder ${supperAdder(10)(20)(30)}")
    println(standardFormater(Math.PI))
    println(preciseFormater(Math.PI))

    println(s"${toCurry(_ + _)(10)(20)}")
    val simpleAdder = fromCurry(superAdder_v1)
    println(s"${simpleAdder(10,20)}")
    println(composedApplication(14)) // 29 = 2 * 14 + 1
    println(aSequencedApplication(14)) // 30 = (14 + 1) * 2


  }
}
