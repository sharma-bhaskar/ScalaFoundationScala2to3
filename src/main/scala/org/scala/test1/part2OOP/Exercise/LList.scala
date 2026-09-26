package org.scala.test1.part2OOP.Exercise

import scala.annotation.tailrec

// singly linked list
// [1,2,3] = [1] -> [2] -> [3] -> []
abstract class LList {

  def head: Int

  def tail: LList

  def isEmpty: Boolean

  def add(element: Int): LList = new Cons(element, this)

}

class EmptyList extends LList {

  override def isEmpty: Boolean = true

  override def head: Int = throw new NullPointerException

  override def tail: LList = throw new NullPointerException

  override def toString: String = "[]"


}

class Cons(override val head: Int, override val tail: LList) extends LList {

  override def isEmpty: Boolean = false

  override def toString: String = {
    @tailrec
    def concatElement(rem: LList, acc: String): String = {
      if (rem.isEmpty) acc
      else concatElement(rem.tail, s"$acc,${rem.head}")
    }

    s"[${concatElement(this.tail, s"$head")}]"
  }

}

object LLlistApp {
  def main(args: Array[String]): Unit = {
    val empty = new EmptyList
    println(empty)
    println(empty.isEmpty)

    val firstThreeElem  = new Cons(1, new Cons(2, new Cons(3, empty)))
    val firstThreeElem_V2 = empty.add(1).add(2).add(3)
    println(firstThreeElem_V2)

    println(firstThreeElem)

  }
}
