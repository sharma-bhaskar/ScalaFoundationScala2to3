package org.scala.test1.part3FunctionalProg

object WhatiSFunction {

  //fp is first class function
  // functional programming is more declareative rather thn just imperative or JVM way
  // FP is the one more tendency is immutability this prefer val over var
  //Functional programming without side effects is called pure functional programming


  // Also Function takes a parameter and return as a func or it can also take function as a parameter and return as value

  //Define a simple function
  // in scala their is a two ways to declare a function so that JVM compiler read as a first class citizens

  //like below

  val function1 = new Function[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  //other way to declare same function like this below

  val function1_v1: Int => Int = (v1: Int) => v1 + 1 // so this is expression way to declare a function rather than abstract method

  //create a abstract for function

  trait MyFunction[A, B] {
    def apply(a: A): B
  }

  //Abstract way to create own function rather depend on the compiler created on
  val myFunction1 = new MyFunction[Int, Int] {
    override def apply(a: Int): Int = a + 1
  }

  //create a function that takes two values and return a result

  val adder: (Int, Int) => Int = (a: Int, b: Int) => a + b

  //create function takes as a parameter and return as a function

  //apply a function twice

  // this is example of function takes parameter and function and return a fucnction
  def applyFtwice(f: Int => Int, x: Int): Int = f(f(x))

  val fTwice: Int => Int = (x: Int) => x + 1

  //in functional programming, a function itself can be treated as a value

  /*
  *   1. value - value
        f:A -> B
        doube(x) = x * 2
        5 --- double --- 10
        Input is a value.
        Output is a value.
        Give value, get value

      2. function - value
         (f:A --> B) --> C
         suppose function as a parameter
         applyTo(10)(f) = f(10)
         So this expect a function with value so lets say
         double(x) = x * 2  this is function
         applyTo(10)(double) = double(10)
         Give function, get value

     3. value -->  function
       f:A -> B -> C
      add(x) = y -> x + y
      add(5) = y -> 5 + y
      this is return new function i.e y - 5 + y
      So this give m A and return B and C
      eg add5(y) = 5 + y
         add(4)(5) = 5 + 4
          A -> B -> C
      Give value, get function

    4. Function -> Function
       fp take fp & return another fp
       twice(f)  -- > this return f
       let inc(x) = x + 1
       then twice(inc) -- x -> inc(inc(x))
       transform  fp --> fp (A -> B) -> (C ->  D)
       Give function, get function
  * */


  // this is a function which take x and double the double
  val double: Int => Int = x => x * 2

  // this is Function -> Value
  // so in this case we need to parse the value that is depend on the outside the function

  val dependedValue = 10

  def apply10f(f: Int => Int): Int = f(dependedValue)

  //take value -- return function
  def add(f: Int => Int)(g: Int => Int): (Int, Int) => Int = (x: Int, y: Int) => f(x) + f(y)

  //take value -- return function this below means Int → Int → Int
  //Give me one Int, and I can produce something that waits for another Int, then produces an Int
  def addMethod(x: Int): Int => Int = y => x + y // also we can write like this def add(x:Int) (y:Int) : Int = x + y

  //but above one is a method with multiple parameter lists.

  // if we need to write value return function example below could be the best
  val add: Int => Int => Int = x => y => x + y

  //eg: of fp taking fp
  val twice: (Int => Int) => Int => Int = f => x => f(f(x))
  val inc: Int => Int = x => x + 1


  //Exercise

  /**
   * Exercises
   *
   *1. A function which takes
   *   2 strings and concatenates them
   *   2.Replace Predicate / Transformer
   *   with the
   *   appropriate function types
   *   if necessary
   *   3.Define a function which takes an int as argument and returns ANOTHER FUNCTION as a result.
   */

  //1 Exercise
  val stringConcat: (String, String) => String = (x: String, y: String) => s"${x + y}"

  // 2. Exercise done in Glist

  //3. take int as argument and return another function as a result
  val functionInt: Int => Int => Int = f => x => f * x

  // call this
  def main(args: Array[String]): Unit = {
    println(s"Abstract way to declare a function: ${function1(10)}")
    println(s"Other way to declare function as expression: ${function1_v1(20)}")
    println(s"Adder function : ${adder(20, 20)}")
    println(s"Apply a function twice : ${applyFtwice(fTwice, 20)}")
    println(s"Apply a function twice : ${applyFtwice(fTwice, 20)}")
    //function composition
    // 1.this is simple where we are passing a value to function and return value
    // f(x) = x * 2
    println(s"Value - Value ${double(10)}")

    // 2. this is Function -> Value
    // suppose it receives another function like this
    // applyTo(10)f = f(10)
    println(s"Function --> Value ${apply10f(double)}")

    // this is value to function
    // can call it currying where one value dependent on other value
    println(s"add method ${addMethod(10)(20)}")
    println(s"Value -> Function ${add(10)(20)}")

    //function to function example

    println(s"Function --> Function ${twice(inc)(10)}")
    println(s"String Concat ${stringConcat("hello", "hi")}")


  }

}
