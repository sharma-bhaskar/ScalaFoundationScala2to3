package org.scala.test1.part2OOP

object AccessModifiers {

  //Scala has private, protected and default access modifiers
  // where protected say, can access within the class or access to inside the class + child class

  // where private says, can access only inside the class outside not allowed
  //eg

  class Person(val name: String) {
    // this is default we can use it everywhere
    def sayHi(): String = s"Hi, my name is $name"

    //protected with policy can only access in class or child class
    protected def sayHiFromProtected(): String = s"Hi, my name is $name"

    //private with policy can only access in class only

    private def watchNetflix():String = "I am binge watching my fav series"
  }

  class KidWithParent(override val name:String, age:Int,momName:String,dadName:String) extends Person(name) {
    val mom = new Person(momName)
    val dad = new Person(dadName)

    def everyOneSayHi():String = s"Hi, my name is ${mom.sayHi()} & ${dad.sayHi()}" //this is allow for default class

    //def everyOneSayHi2(): String = s"Hi, my name is ${mom.sayHiFromProtected()} & ${dad.sayHiFromProtected()}" //this is not allow for default class

  }

  //now protected with the child class
  class Kid(override val name: String, age: Int) extends Person(name) {
    def greetPolicy(): String = sayHiFromProtected() + ", I love to play"
  }

  val aPerson = new Person("Alice")
  val aKid = new Kid("Bob", 12)

  def main(args: Array[String]): Unit = {
    println(aPerson.sayHi())
    // println(aPerson.sayHiFromProtected) //this is not allowed as method is protected
    println(aKid.greetPolicy())

  }

}
