package org.scala.test1.part2OOP

object MethodNotation {

  class Person(val name: String, val age: Int, val favoriteMovie: String) {
    def likes(movie: String): Boolean = movie == favoriteMovie

    // infix operator
    // in scala three operator one such is infix where if method
    // has one parameter we can mark it infix def and thn we can call without . operator
    // like example below

    infix def likesMovies(movie: String): Boolean = movie == favoriteMovie
    // other examples

    infix def +(person: Person): String = {
      s"${this.name} is hanging out with ${person.name}"

      //similar like we can define different operator likewise

    }

    // prefix position
    // unary ops: -, +, ~, !
    // these unary operator dont have paranthesis
    def unary_- : String = s"$name alter ego"

    //PostFix operator this is without argument methods such as

    // but postfix is very discourage as it uses with dot and so every method we uses with dot only
    def isAlive: Boolean = false

    //Special method apply

    def apply(): String = s"$name is calling special method apply"

    infix def +(nickName: String): Person = new Person(s"$name $nickName", age, favoriteMovie)

    def unary_+ : Person = new Person(name, age = age + 1, favoriteMovie)

    def apply(arg: Int): String = s"${this.name} watched Inception $arg times"


  }

  val mary = new Person("Mary", 34, "Inception")
  val john = new Person("John", 35, "Runners")

  /**
   * Exercises
   *  - a + operator on the Person class that returns a person with a nickname
   *    mary + "the rockstar" => new Person("Mary the rockstar", _, _)
   *  - a UNARY + operator that increases the person's age
   *    +mary => new Person(_, _, age + 1)
   *  - an apply method with an int arg
   *    mary.apply(2) => "Mary watched Inception 2 times"
   */
  def main(args: Array[String]): Unit = {
    println(mary.likes("Fight Club"))
    //infix call
    println(mary likesMovies "fightClub")
    println(mary + john)
    //prefix
    println(-mary)
    //postfix
    println(mary.isAlive)

    //special method

    println(mary.apply()) //or
    println(mary()) //both are same to call this method

    //exercise
    //1 add nickname to the mary
    val marytheRockStar = mary + "therockstar"
    println(marytheRockStar.name)
    // increase age of the mary
    val ageIncrease = +mary
    println(ageIncrease.age)
    println(mary(3))



  }

}
