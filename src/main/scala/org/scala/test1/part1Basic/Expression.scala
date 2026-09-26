package org.scala.test1.part1Basic

object Expression {

  //Expression in scala are structure that can be evaluted to a value

  val meaningOfLife = 42
  //example mathematical model like +, - , x, / and bitwise |, &, <<, >>, >>>,

  val mathExpression: Int = 2 + 3 + 4
  // so if you see above expression is like we are calculating a value
  // that is combination of expression and final result so its returning the result as
  // Int

  // Comparsion expression <, <=, >, >=, ==,!=

  val equalityTest = 1 == 2

  //boolean expression !, ||, &&

  val nonEquality = !equalityTest

  //So in programming world there are two types of things
  // first instruction and expression, instruction generally used in python or javascript
  // where we provide instruction like below
  // age = 25
  //print(age)

  // so expression is evalution criteria rather instruction are expecting the values
  // so in scala we think in term of expression rather thn intruction

  // if are expression

  val aCondition = true
  val onIfExpression = if (aCondition) 42 else 45

  // also in code blocks

  val aCodeBlock = {
    val localVariable = 45  // this is local variable
    localVariable + 2 //  this is final result of the variable
  }

  //Exercise

  //1. Ex
  val someValue: Boolean = {
    2 < 3
  }

  //2.
  val someOtherValues = {
    if(someValue) 23 else 34
    43
  }

  //3. // type unit is void is other lang so these are the void function
  val yetAnotherExpression : Unit = println("Scala")



  def main(args: Array[String]): Unit = {
    println(meaningOfLife)

    println(someValue) // true
    println(someOtherValues) //43
    println(yetAnotherExpression) // Unit
  }

}
