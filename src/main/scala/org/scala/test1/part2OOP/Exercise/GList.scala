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

/**1. LList exercises
 *    - foreach(A => Unit): Unit
 *      [1,2,3].foreach(x => println(x))
 *
 *    - sort((A, A) => Int): LList[A]
 *      [3,2,4,1].sort((x, y) => x - y) = [1,2,3,4]
 *      (hint: use insertion sort)
 *
 *    - zipWith[B](LList[A], (A, A) => B): LList[B]
 *      [1,2,3].zipWith([4,5,6], x * y) => [1 * 4, 2 * 5, 3 * 6] = [4, 10, 18]
 *
 *    - foldLeft[B](start: B)((A, B) => B): B
 *      [1,2,3,4].foldLeft[Int](0)(x + y) = 10
 *      0 + 1 = 1
 *      1 + 2 = 3
 *      3 + 3 = 6
 *      6 + 4 = 10
 */

abstract class GList[A] {
  def head: A

  def tail: GList[A]

  def isEmpty: Boolean

  def add(elem: A): GList[A] = NonEmpty(elem, this)

  //concatention
  infix def ++(anotherList: GList[A]): GList[A]

  def map[B](transformer: A => B): GList[B]

  def filter(predicate: A => Boolean): GList[A]

  def flatMap[B](transformer: A => GList[B]): GList[B]

  /*
  *  - foreach(A => Unit): Unit
  *      [1,2,3].foreach(x => println(x))*/
  def foreach(f: A => Unit): Unit

  // *    - sort((A, A) => Int): LList[A]
  // *      [3,2,4,1].sort((x, y) => x - y) = [1,2,3,4]
  // *      (hint: use insertion sort)

  def sort(f: (A, A) => Int): GList[A]

  // *    - zipWith[B](LList[A], (A, A) => B): LList[B]
  // *      [1,2,3].zipWith([4,5,6], x * y) => [1 * 4, 2 * 5, 3 * 6] = [4, 10, 18]

  def zipWith[B, T](list: GList[T], zip: (A, T) => B): GList[B]

  // *    - foldLeft[B](start: B)((A, B) => B): B
  // *      [1,2,3,4].foldLeft[Int](0)(x + y) = 10
  // *

  def foldLeft[B](start: B)(oper: (B, A) => B): B

}

//2.Replace Predicate / Transformer
//  *
//with the
//  * appropriate function types
//*
//if necessary

trait FunctionPredicate[T] {
  def test(f: T => Boolean): Boolean

  val test: T => Boolean
}

trait Predicate[T] {
  def test(element: T): Boolean
}

//function type transformer

trait FunctionTrasnformer[A, B] {
  def transformer(a: A => B): B

  val transformer: A => B
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

  def map[B](transformer: A => B): GList[B] = Empty[B]()

  def filter(predicate: A => Boolean): GList[A] = Empty[A]()

  def flatMap[B](transformer: A => GList[B]): GList[B] = new Empty[B]

  override def foreach(f: A => Unit): Unit = ()

  override def sort(f: (A, A) => Int): GList[A] = this

  override def zipWith[B, T](list: GList[T], zip: (A, T) => B): GList[B] =
    if (!list.isEmpty) throw new IllegalArgumentException("Zipping lists of nonequal length")
    else Empty()


  override def foldLeft[B](start: B)(oper: (B, A) => B): B = start
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
  override def map[B](transformer: A => B): GList[B] =
    NonEmpty(transformer(head), tail.map(transformer))

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
  override def filter(predicate: A => Boolean): GList[A] = if (predicate(head))
    NonEmpty(head, tail.filter(predicate)) else tail.filter(predicate)


  /*
    * [1,2,3].flatMap(n => [n, n+1]) => [1,2, 2,3, 3,4] =
   *  [1,2] ++ [2,3]
    [1,2,2,3] ++ [3]
   [1,2, 2,3, 3,4] ++ []
   [1,2, 2,3, 3,4]
   *
   */
  override def flatMap[B](transformer: A => GList[B]): GList[B] =
    transformer(head) ++ tail.flatMap(transformer)

  //- foreach(A => Unit): Unit
  // *      [1,2,3].foreach(x => println(x))
  override def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }

  // *    - sort((A, A) => Int): LList[A]
  // *      [3,2,4,1].sort((x, y) => x - y) = [1,2,3,4]
  // *      (hint: use insertion sort)

  /*
        compare = x - y
        insert(3, [1,2,4]) =
        Cons(1, insert(3, [2,4])) =
        Cons(1, Cons(2, insert(3, [4]))) =
        Cons(1, Cons(2, Cons(3, [4]))) = [1,2,3,4]
       */
  override def sort(compare: (A, A) => Int): GList[A] = {
    def insertionSort(elem: A, sortedList: GList[A]): GList[A] = {
      if (sortedList.isEmpty) NonEmpty(elem, Empty())
      else if (compare(elem, tail.head) <= 0) NonEmpty(elem, sortedList)
      else NonEmpty(sortedList.head, insertionSort(elem, sortedList.tail))
    }

    val sortedTail = tail.sort(compare)
    insertionSort(head, sortedTail)
  }

  override def zipWith[B, T](list: GList[T], zip: (A, T) => B): GList[B] =
    if (list.isEmpty) throw new IllegalArgumentException("Zipping lists of nonequal length")
    else NonEmpty(zip(head, list.head), tail.zipWith(list.tail, zip))


  /*
    [1,2,3,4].foldLeft(0)(x + y)
    = [2,3,4].foldLeft(1)(x + y)
    = [3,4].foldLeft(3)(x + y)
    = [4].foldLeft(6)(x + y)
    = [].foldLeft(10)(x + y)
    = 10
   */

  override def foldLeft[B](start: B)(oper: (B, A) => B): B =
    tail.foldLeft(oper(start, head))(oper)
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

    val someStrings = NonEmpty("dog", NonEmpty("cat", NonEmpty("crocodile", Empty())))
    println(someStrings)
    
    val Doubler: Int => Int = (x: Int) => x * 2

    val noDouber = nonEmpty.map(Doubler)
    println(noDouber)

    val evenPredicte2: Int => Boolean = (x: Int) => x % 2 == 0

    val doublerList: Int => GList[Int] = (a: Int) =>  NonEmpty[Int](a, new NonEmpty[Int](a + 1, new Empty[Int]))


    val nestedNo = nonEmpty.map(doublerList)
    println(nestedNo)

    val filterNo = nonEmpty.filter(evenPredicte2)
    println(filterNo)

    //concat list

    val concatList = nonEmpty ++ firstThreeElem_V2
    println(concatList)

    val flatMapList = nonEmpty.flatMap(doublerList)
    println(flatMapList)

    // HOFs exercises testing
    firstThreeElem_V2.foreach(println)
    println(firstThreeElem_V2.sort(_ - _))
    val zippedList = firstThreeElem_V2.zipWith[String, String](someStrings, (number, string) => s"$number-$string")
    println(zippedList)
    println(firstThreeElem_V2.foldLeft(0)(_ + _))


  }
}
