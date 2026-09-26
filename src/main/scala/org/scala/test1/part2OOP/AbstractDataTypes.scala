package org.scala.test1.part2OOP

object AbstractDataTypes {

  //Abstract data types in scala is like provide an un - implementation of method and field and child class will provide the impl of that thing
  // Abstract data is also a trait where we provide multiple abstract method and some implementation method as well

  //this is abstract class where dont have any implementation
  abstract class Animal {
    val creatureType: String

    def eat(): Unit

    //this is optional but we can provide impl of method as well
    def preferredMeal(): String = "Anything"
  }

  class Dog extends Animal {
    override val creatureType: String = "Dog"

    override def eat(): Unit = println("I can eat nothing")

    override def preferredMeal(): String = "I have preferrence"
  }

  // Now trait is like to describe behavior of the class

  trait Carnivore {
    def eat(animal: Animal):Unit // i can provide impl but i dont want to
  }

  class TRex extends Carnivore {
    override def eat(animal: Animal): Unit = println("i can eat animal")
  }

  // diff btw trait and animal is the multiple inheritance

  trait ColdBlooded
  class Crocodile extends Animal with Carnivore with ColdBlooded {

    override val creatureType: String = "Crocodile"

    override def eat(): Unit = println("I can eat anything")

    override def eat(animal: Animal): Unit = println("I can eat anything, animal")
  }
  // so this is what we have multiple inheritance through the trait so traits is behavior

  //Data types
  /*
  * Any --
      AnyVal -- this is premitive - Int, Boolean, Char ----
      AnyRef -- this is AnyRef of the class like extend generally default extend of anyRef and String, Integer so some other classes

  scala.Null so this is null reference of the class

  NOTHING is special type where you can return NOTHING if you dont have anything of the field or method scala.Nothing
  *
  * */

}
