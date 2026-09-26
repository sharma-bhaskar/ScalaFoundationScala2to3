package org.scala.test1.part2OOP.Exercise

import scala.annotation.tailrec

/**
 * Exercise: LList extension
 *
 *1.  Generic trait Predicate[T] with a little method test(T) => Boolean
 *2.  Generic trait Transformer[A, B] with a method transform(A) => B
 *3.  LList:
 *- map(transformer: Transformer[A, B]) => LList[B]
 *- filter(predicate: Predicate[A]) => LList[A]
 *- flatMap(transformer from A to LList[B]) => LList[B]
 *
 * class EvenPredicate extends Predicate[Int]
 * class StringToIntTransformer extends Transformer[String, Int]
 *
 * [1,2,3].map(n * 2) = [2,4,6]
 * [1,2,3,4].filter(n % 2 == 0) = [2,4]
 * [1,2,3].flatMap(n => [n, n+1]) => [1,2, 2,3, 3,4]
 */
abstract class GList[A] {
  def head: A

  def tail: GList[A]

  def isEmpty: Boolean

  def add(elem: A): GList[A] = NonEmpty(elem, this)

  //concatention
  infix def ++(anotherList: GList[A]): GList[A]

  def map[B](transformer: Transformer[A, B]): GList[B]

  def filter(predicate: Predicate[A]): GList[A]

  def flatMap[B](transformer: Transformer[A, GList[B]]): GList[B]
}

trait Predicate[T] {
  def test(element: T): Boolean
}

trait Transformer[A, B] {
  def transformer(a: A): B
}

class EvenPredicte extends Predicate[Int] {

  override def test(element: Int): Boolean = element % 2 == 0
}

//AonmyousClass of predicate

class StringToIntTransformer extends Transformer[String, Int] {

  override def transformer(a: String): Int = a.toInt
}

class DoublerList extends Transformer[Int, GList[Int]] {

  override def transformer(a: Int): GList[Int] = new NonEmpty[Int](a, new NonEmpty[Int](a + 1, new Empty[Int]))
}

class Doubler extends Transformer[Int, Int] {
  override def transformer(value: Int): Int = value * 2
}

case class Empty[A]() extends GList[A] {

  def head: A = throw new NoSuchElementException

  def tail: GList[A] = throw new NoSuchElementException

  def isEmpty: Boolean = true

  override def toString: String = "[]"

  infix def ++(anotherList: GList[A]): GList[A] = anotherList

  def map[B](transformer: Transformer[A, B]): GList[B] = Empty[B]()

  def filter(predicate: Predicate[A]): GList[A] = Empty[A]()

  def flatMap[B](transformer: Transformer[A, GList[B]]): GList[B] = new Empty[B]
}

case class NonEmpty[A](override val head: A, override val tail: GList[A]) extends GList[A] {

  override def isEmpty: Boolean = false

  /*
     example
     [1,2,3] ++ [4,5,6]
     new Cons(1, [2,3] ++ [4,5,6]) =
     new Cons(1, new Cons(2, [3] ++ [4,5,6])) =
     new Cons(1, new Cons(2, new Cons(3, [] ++ [4,5,6]))) =
     new Cons(1, new Cons(2, new Cons(3, [4,5,6]))) =
     [1,2,3,4,5,6]
    */

  /**
   * [1,2,3] ++ [4,5,6]
   * new NonEmpty(1,[2,3] ++ [4,5,6])
   * new NonEmptu(1,new NonEmpty(2, [3] ++ [4,5,6])
   * new neEmptu(1,new NonEmpty(2, new nonEmpty(3,[] ++ [4,5,6])
   * new nonEmpty(1,new NoneEmpty(2,new nonEmpty(3, [4,5,6])
   *
   * */
  override infix def ++(anotherList: GList[A]): GList[A] =
    NonEmpty(head, tail ++ anotherList)

  override def toString: String = {
    @tailrec
    def concatList(rem: GList[A], acc: String): String = {
      if (rem.isEmpty) acc
      else concatList(rem.tail, s"$acc, ${rem.head}")
    }

    s"[${concatList(this.tail, s"$head")}]"
  }

  /* *
 * [1,2,3].map(n * 2) = [2,4,6]
 * [1,2,3,4].filter(n % 2 == 0) = [2,4]
 * [1,2,3].flatMap(n => [n, n+1]) => [1,2, 2,3, 3,4]
 */
  /*
      example
      [1,2,3].map(n * 2) =
      new Cons(2, [2,3].map(n * 2)) =
      new Cons(2, new Cons(4, [3].map(n * 2))) =
      new Cons(2, new Cons(4, new Cons(6, [].map(n * 2)))) =
      new Cons(2, new Cons(4, new Cons(6, [])))) =
      [2,4,6]
     */
  override def map[B](transformer: Transformer[A, B]): GList[B] =
    NonEmpty(transformer.transformer(head), tail.map(transformer))

  /**
   *  * [1,2,3,4].filter(n % 2 == 0) = [2,4]
   *  *
   *  *
   *
   *
   * */
  /*
    example
    [1,2,3].filter(n % 2 == 0) =
    [2,3].filter(n % 2 == 0) =
    new Cons(2, [3].filter(n % 2 == 0)) =
    new Cons(2, [].filter(n % 2 == 0)) =
    new Cons(2, []) =
    [2]
   */
  override def filter(predicate: Predicate[A]): GList[A] = if (predicate.test(head))
    NonEmpty(head, tail.filter(predicate)) else tail.filter(predicate)


  /*
    * [1,2,3].flatMap(n => [n, n+1]) => [1,2, 2,3, 3,4] =
   *  [1,2] ++ [2,3]
    [1,2,2,3] ++ [3]
   [1,2, 2,3, 3,4] ++ []
   [1,2, 2,3, 3,4]
   *
   */
  override def flatMap[B](transformer: Transformer[A, GList[B]]): GList[B] =
    transformer.transformer(head) ++ tail.flatMap(transformer)
}


object GlistImple {

  def main(args: Array[String]): Unit = {

    val empty = Empty[Int]()
    println(empty)

    val nonEmpty = NonEmpty(1, NonEmpty(2, NonEmpty(3, Empty())))

    val firstThreeElem_V2 = empty.add(1).add(2).add(3)
    println(nonEmpty)
    println(firstThreeElem_V2)

    val evenPredicte = new Predicate[Int] {
      override def test(element: Int): Boolean = element % 2 == 0
    }
    val transformerToInt = new Transformer[String, Int] {
      override def transformer(a: String): Int = a.toInt
    }

    val noDouber = nonEmpty.map(Doubler())
    println(noDouber)


    val nestedNo = nonEmpty.map(DoublerList())
    println(nestedNo)

    val filterNo = nonEmpty.filter(evenPredicte)
    println(filterNo)

    //concat list

    val concatList = nonEmpty ++ firstThreeElem_V2
    println(concatList)

    val flatMapList = nonEmpty.flatMap(DoublerList())
    println(flatMapList)

  }
}
