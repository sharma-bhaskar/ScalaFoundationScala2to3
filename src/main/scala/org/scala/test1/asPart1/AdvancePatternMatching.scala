package org.scala.test1.asPart1

object AdvancePatternMatching {


  /*
  *  Pattern matching works on more and advance topic below as we covered as of the
  *  - constant
  *  - object
  *  - wildCards
  * - infix patterns
  * - lists
  * - case class
  * */

  // we can create our own pattern matching method using unapply method

  class Person(val name: String, val age: Int)

  val bob = new Person("Bob", 25)

  object Person {
    def unapply(person: Person): Option[(String, Int)] = {
      if (person.age < 18) None
      else Some((person.name, person.age))
    }

    def unapply(age: Int): Option[(String)] = {
      if (age < 21) Some("Minor")
      else Some("Legal age to drink")
    }
  }

  val bobPM = bob match {
    case Person(name, age) => s"Hi there, I'm $name" // as intially we dont create unapply extractor that is my its throwing error
  }

  // suppose we have different pattern matching like for int, boolean and others  in this case we need to add unapply for int variable

  val bobAge = bob.age match {
    case Person(age) => s"Bob's legal drinking status is $age"
  }
  // Pattern apply own type of own method
  // unapply method help us to extractor the element from the bounded type of DS and unapply take two either None and Some so it not always we need to return
  // option in other case we need to return empty of some like below boolean example

  //boolean patterns

  object even {
    def unapply(n: Int): Boolean = n % 2 == 0
  }

  object singleDigits {
    def unapply(n: Int): Boolean = n > -10 && n < 10
  }

  val n = 43

  val mathProperty = n match {
    case even() => "an even"
    case singleDigits() => "a one digit numer"
    case _ => "No special properties"
  }

  //infix pattern matching

  infix case class OR[A, B](a: A, b: B)

  val aEither = OR(2, "Two")

  val humDes = aEither match {
    case number OR string => s"$number is written $string"
  }
  // this infix type available for other types

  val aList = List(1, 2, 3)

  // decomposing sequences
  val listPM = aList match {
    case 1 :: rest => "a list starting with 1"
    case _ => "some uninteresting list"
  }

  //create my list 

  abstract class MyList[A] {
    def head: A = throw new NoSuchElementException

    def tail: MyList[A] = throw new NoSuchElementException
  }

  case class Empty[A]() extends MyList[A]

  case class Cons[A](override val head: A, override val tail: MyList[A]) extends MyList[A]

  //create list
  //nnow create extractor

  object MyList {
    def unapplySeq[A](list: MyList[A]): Option[Seq[A]] = if (list == Empty()) Some(Seq.empty)
    else unapplySeq(list.tail).map(restSeq => list.head +: restSeq)
  }

  val varargCustom = myList match {
    case MyList(1, _*) => "list starting with 1"
    case _ => "some other list"
  }

  val myList = Cons(1, Cons(2, Cons(3, Empty())))

  // custom return type for unapply

  abstract class Wrapper[T] {
    def isEmpty: Boolean

    def get: T
  }

  // compiler will decide at run time

  object PersonWrapper {
    def unapply(person: Person): Wrapper[String] =

      new Wrapper[String] {
        override def isEmpty: Boolean = false

        override def get: String = person.name
      }
  }

  val weirdPersonPM = bob match {
    case PersonWrapper(name) => s"Hey my name is $name"
  }


  def main(args: Array[String]): Unit = {
    println(bobPM)
    println(bobAge)
    println(mathProperty)
    println(humDes)
    println(myList)
    println(varargCustom)
    println(weirdPersonPM)
  }

}
