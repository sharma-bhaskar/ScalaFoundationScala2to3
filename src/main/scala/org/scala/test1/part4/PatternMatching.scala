package org.scala.test1.part4

import scala.util.Random

object PatternMatching {

  //pattern matching is powerfull tool to check the pattern on cases and behaviour of the cases

  // so pattern matching is the steroid on switch haha

  val random = new Random()

  val aValue = random.nextInt()

  val des = aValue match {
    case 1 => "1"
    case 2 => "2"
    case 3 => "3"
    case _ => "I am default " //this is case default _ underscore where if any patter didn't match it uses this
  }

  //pattern matching best works on case classes

  case class Person(name: String, age: Int)

  val bob = Person("bob", 23)

  //pattern matching guard rails
  val greeting = bob match {
    case Person(name, age) if age < 18 => "I am not allowed to tell you, who am i "
    case Person(n, a) => s"I am $n and my is $a"
    case _ => "Hey, are you searching some person???"
  }

  /*
      Patterns are matched in order: put the most specific patterns first.
      What if no cases match? MatchError
      What's the type returned? The lowest common ancestor of all types on the RHS of each branch.
     */
  // pattern matching on sealed traits
  sealed trait Animal

  case class Dog(name: String) extends Animal

  case class Cat(name: String) extends Animal

  val dog = Dog("Alex")

  val validDog: String = dog match {
    case Dog(name) => s"my name is $name"
  }

  /**
   * Exercise
   * show(Sum(Number(2), Number(3))) = "2 + 3"
   * show(Sum(Sum(Number(2), Number(3)), Number(4)) = "2 + 3 + 4"
   * show(Prod(Sum(Number(2), Number(3)), Number(4))) = "(2 + 3) * 4"
   * show(Sum(Prod(Number(2), Number(3)), Number(4)) = "2 * 3 + 4"
   */
  sealed trait Expr

  case class Number(n: Int) extends Expr

  case class Sum(e1: Expr, e2: Expr) extends Expr

  case class Prod(e1: Expr, e2: Expr) extends Expr

  def show(expr: Expr): String = expr match {
    case Number(n) => s"$n"
    case Sum(e1, e2) => s"${show(e1)} + ${show(e2)}"
    case Prod(e1, e2) => {
      def mayShowParantheis(expr: Expr): String = expr match {
        case Prod(_, _) => show(expr)
        case Number(n) => show(expr)
        case Sum(_, _) => s"(${show(expr)})"
      }

      mayShowParantheis(e1) + " * " + mayShowParantheis(e2)

    }
  }

  def main(args: Array[String]): Unit = {
    println(validDog)
    println(greeting)

    println(show(Sum(Number(2), Number(3))))
    println(show(Sum(Sum(Number(2), Number(3)), Number(4))))
    println(show(Prod(Sum(Number(2), Number(3)), Number(4))))
    println(show(Sum(Prod(Number(2), Number(3)), Number(4))))

  }
}
