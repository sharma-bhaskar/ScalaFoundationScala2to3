package org.scala.test1.part2OOP

object Objects {

  //What is objects in scala ?
  // object is the singleton instance of the code where you can create only single instance of the object
  // like singleton pattern from the other language in the scala
  // object has same behavior like classes where we can declare method and variable also

  object MySingleton

  val theSingleTon = MySingleton
  val anotherSingleton = MySingleton
  val checkSingleton = theSingleTon == anotherSingleton
  //they all are same
  // in singleton object we can declare the fields and methods also

  object MySingletonCheck {
    val aField = 34

    def aMethod(x: Int): Int = x + 1
  }

  //objects can have field & methods
  val aSingleTonField: Int = MySingletonCheck.aField
  val aMethodCall: Int = MySingletonCheck.aMethod(4)

  //companion object where we can define same class name and object

  class Person(name: String) {
    def sayHi() = s"Hi, my name is $name"
  }

  object Person {
    //in object it use to define the constant and bit check functionality
    val N_EYES = 2

    def canFly: Boolean = false
  }

  //class usage
  val mary = new Person("Mary")
  val mary_v2 = new Person("Mary")
  val checkIb = mary eq mary_v2 //using eq this check the object reference two object is same or not
  val maryGreeting = mary.sayHi()

  //object usage
  val nEyes = Person.N_EYES
  val canFl = Person.canFly

  //equality check
  // 1. using eq this check the object reference two object is same or not  so basically its uses to check mem
  // 2. using equal to check the samenes of the value or data  and its uses to check sameness

  def main(args: Array[String]): Unit = {

  }

}
