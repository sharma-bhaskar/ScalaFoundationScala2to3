package org.scala.test1.asFP

object PartialFunctionExample {

  // Partial: A Function with an Incomplete Domain
  // function that define subset of x rather than the full definition
  //eg:

  //normal function
  val aFunc: Int => Int = x => x + 1
  // often time we need to match with the function args with bunch of cases

  val aFussyFunction: Int => Unit = (x: Int) => {
    if (x == 1) 1
    else if (x == 2) 2
    else throw new RuntimeException("No suitable input")

  }


  //another way to write this with pattern matching

  val afussyFunction_v2 = (x: Int) => x match {
    case 1 => 1
    case 2 => 2
    case 3 => 3
  }

  //fuction only applicable for subset of function

  val paritialFunction: PartialFunction[Int, Int] = {
    case 1 => 1
    case 2 => 2
    case 3 => 3
  }

  // pf is the extension of the normal function type beside that we can check pf is applicable for the some def

  //utilities methods of PFS
  val canCallOn37 = paritialFunction.isDefinedAt(23)

  //chain of a partial function

  val anotherPF: PartialFunction[Int, Int] = {
    case 43 => 43
  }

  val chainPartialFunction = paritialFunction.orElse[Int, Int](anotherPF)

  //HOFS function accept PF as argument

  val aList = List(1, 2, 3, 4)
  val aChangedList = aList.map(x => x match {
    case 1 => 2
    case 2 => 3
    case 3 => 3
    case _ => 0
  })

  val aChangedList_V2 = aList.map {
    case 1 => 2
    case 2 => 3
    case 3 => 3
    case _ => 0
  }

  case class Person(name: String, age: Int)

  val someKids = List(
    Person("Alice", 3),
    Person("Bobbie", 5),
    Person("Jane", 4)
  )

  val kidsGrowingUp = someKids.map {
    case Person(name, age) => Person(name, age + 1)
  }


  def main(args: Array[String]): Unit = {
    println(chainPartialFunction)
    println(kidsGrowingUp)
  }

}
