package org.scala.test1.part2OOP

object Inheritance {

  //Inheritance is capabilities to, inherit some properties from superclass to subclass

  class Animal {
    val creatureTypes = "wild"

    def eat(): Unit = println("I dont have specific eating pattern")
  }

  class Cat extends Animal {
    def crunch(): Unit = {
      eat()
      println("I drink milk only")
    }
  }


  //How inheritance works in constructor types for that we need overload constructor from supertype
  class Person(name: String, age: Int) {
    //define auxillary constructors
    def this(name: String) = this(name, 0)
  }

  class Adult(name: String, age: Int, idCard: Int) extends Person(name) // so in this case if we are using constructor thn it should be from supertype constructor

  //Overriding is override the method of super class into the subtype class

  class Dog extends Animal {
    override val creatureTypes: String = "dog"

    override def eat(): Unit = println("I am dog I cant eat anything")

    //some types where we need overriding to define
    // method like toString, equal, hashcode is already part of these systems

    override def toString: String = "dog"
  }

  //subtype polymorphism

  val cat = new Cat()

  val dog: Animal = new Dog()

  //this is left hand rule where left hand side superclass and right we take subtype

  class Crocodile extends Animal {
    override val creatureTypes: String = "very wild"

    override def eat(): Unit = println("I can eat anything, I am crocodile")

    //method overloading 

    def eat(dog: Dog): Unit = println("I can eat dog")
    def eat(person: Person): Unit = println("I can eat person as well")
    def eat(dog: Dog,person: Person): Unit = println("I can eat both")
    
  }

  def main(args: Array[String]): Unit = {
    cat.crunch()
    dog.eat()
    println(dog)

  }
}
