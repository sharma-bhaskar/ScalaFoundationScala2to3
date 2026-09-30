package org.scala.test1.part3FunctionalProg.Practice

/**
 * * 2. A small collection of AT MOST ONE element - Maybe[A]
 *    *  - map
 *    *  - flatMap
 *    *  - filter */
abstract class MayBe[A] {

  def map[B](f: A => B): MayBe[B]

  def filter(f: A => Boolean): MayBe[A]

  def flatMap[B](f: A => MayBe[B]): MayBe[B]

}

class MaybeNot[A] extends MayBe[A] {

  override def map[B](f: A => B): MayBe[B] = MaybeNot[B]

  override def filter(f: A => Boolean): MayBe[A] = this

  override def flatMap[B](f: A => MayBe[B]): MayBe[B] = MaybeNot[B]()
}

case class Just[A](value: A) extends MayBe[A] {

  override def map[B](f: A => B): MayBe[B] = Just(f(value))

  override def filter(predicate: A => Boolean): MayBe[A] = if (predicate(value)) this else MaybeNot[A]()

  override def flatMap[B](f: A => MayBe[B]): MayBe[B] = f(value)
}

object Main {

  val mayBe1: MayBe[Int] = Just(3)
  val maybe2: MayBe[Int] = Just(4)
  val maybe3: MayBe[Int] = MaybeNot[Int]()

  def main(args: Array[String]): Unit = {
    val maybeInt: MayBe[Int] = Just(3)
    val maybeInt2: MayBe[Int] = MaybeNot()
    val maybeIncrementedInt = maybeInt.map(_ + 1)
    val maybeIncrementedInt2 = maybeInt2.map(_ + 1)
    println(maybeIncrementedInt)
    println(maybeIncrementedInt2)

    val maybeFiltered = maybeInt.filter(_ % 2 == 0)
    println(maybeFiltered)

    val maybeFlatMapped = maybeInt.flatMap(number => Just(number * 3))
    println(maybeFlatMapped)


  }
}
