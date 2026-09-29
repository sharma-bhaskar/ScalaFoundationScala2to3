package org.scala.test1.part3FunctionalProg

object AnonymousFunction {

  //define a doubler function

  val doubler: Int => Int = (x: Int) => x * 2

  //these are the lambda expression
  val adder: (Int, Int) => Int = (x: Int, y: Int) => x + y

  //zero type argument

  val zeroArgument: () => Int = () => 45

  //stringToInt

  val stringToInt: String => Int = (str: String) => {
    str.toInt
  }

  //type infers

  val doubler_v3: Int => Int = x => x * 2

  val adder_v2: (Int, Int) => Int = (x, y) => x + y

  // shortest lambdas
  val doubler_v4: Int => Int = _ * 2 // x => x * 2
  val adder_v3: (Int, Int) => Int = _ + _ // (x, y) => x + y

  //  //3. take int as argument and return another function as a result

  val adder_v4: Int => Int => Int = x => y => x + y


  def main(args: Array[String]): Unit = {
    println(adder(10, 30))
    println(zeroArgument) //this is the lambda expression and anonymous class that insitated here
    println(zeroArgument()) //this is calling apply function

    println(adder_v4(10)(10))

  }

}
