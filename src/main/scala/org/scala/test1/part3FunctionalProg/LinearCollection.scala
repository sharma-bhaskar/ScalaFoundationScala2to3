package org.scala.test1.part3FunctionalProg

import scala.util.Random

object LinearCollection {

  // So linear collection is so many types like Seq,List,Array,Vector,Set,Ranges

  // in this most of the immutable except Array where array dont give a new Array so its a mutable seq

  // Seq this is trait - well define ordering + indexing
  // Also most of the Collection is Companion object in Scala
  // Also all the Seq support the HOF

  def testSeq(str: String) = {

    println(str)
    val aSeq = Seq(1, 2, 3, 4, 5)
    println(aSeq)

    val thirdElement = aSeq(2)
    println(thirdElement)
    val reversed = aSeq.reverse
    println(reversed)
    val concat = aSeq ++ Seq(7, 8)
    println(concat)
    val sortedSeq = aSeq.sorted
    println(sortedSeq)
    val aSeqInc = aSeq.map(_ + 1)
    println(aSeqInc)
    val sSeqFlat = aSeq.flatMap(x => Seq(x, x + 1))
    println(sSeqFlat)
    val aSeqFilter = aSeq.filter(_ % 2 == 0)
    println(aSeqFilter)
  }

  def testList(string: String) = {

    println(string)
    val aList = List(1, 2, 3, 4, 5)
    println(aList)

    val firstElement = aList.head
    println(firstElement)
    val restList = aList.tail

    //prepending in a list
    val prependList = aList +: List(9, 0)
    // also can use like this
    val prependList_v2 = aList :: List(10, 0)
    println(prependList_v2)
    println(prependList)
    // as you noticed this is giving a new list as its immutable
    //appending in a list
    val postList = aList :+ List(10, 11)
    println(postList)

    //calculate the sum of all the element in the list
    val sumList = aList.foldLeft(0)(_ + _)
    println(sumList)
    //convert the list in string
    val aStringList = aList.mkString(",")
    println(aStringList)
    //utilities method in the list
    val scalax = List.fill(3)("Scala")
    println(scalax)
  }

  def testRanges(str: String) = {
    println(str)
    val aRanges = 1 to 10

    // all the api is same as seq
    println(aRanges)
    (1 to 10).foreach(x => println(x))
  }

  def testArray(str: String) = {
    println(str)
    //Companion object with mutable declaration
    val aArray = Array(1, 2, 3, 4, 5)

    val aSeq = aArray.toIndexedSeq //convert it to the Seq
    //we can update index of variable in the array
    aArray.update(1, 6)
    println(aArray.mkString("Array(", ", ", ")"))
  }

  def testVector(str: String) = {
    val aVector = Vector(1, 2, 3, 4, 5, 6)
    //all the same api but it quite fast as compare to list and its lock safe also
    // in the case of millions of record its a safe prefer Vector
    println(aVector)
  }
  //small benchmarking of List and Vector

  def smallBenchMark = {
    val maxRun = 1000
    val maxCapacity = 1000000

    def getWriteTime(coll: Seq[Int]): Double = {
      val random = new Random()
      val times = for {
        i <- 1 to maxRun
      } yield {
        val index = random.nextInt(maxCapacity)
        val element = random.nextInt()
        val currentTime = System.nanoTime()

        val updatedCollection = coll.updated(index, element)

        System.nanoTime() - currentTime
      }
      times.sum * 1.0 / maxRun
    }

    val numbersList = (1 to maxCapacity).toList
    val numbersVector = (1 to maxCapacity).toVector

    println(getWriteTime(numbersList))
    println(getWriteTime(numbersVector))
  }

  def testSet() = {
    //set is the implementation of HashSet
    // this is also an companion object

    val aSet = Set(1, 2, 3, 4, 5, 6)
    // equal and hashcode = hashSet
    // API's  in the set
    val contains = aSet(2)
    val addSet = aSet + (1, 2, 3, 4, 5, 5, 6, 7)

    val aSmallerSet = aSet - (5, 6)
    println(aSmallerSet)
    //concatenation

    val anotherSet = Set(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val muchBigSet = aSet ++ anotherSet
    println(muchBigSet)

    val unionSet = aSet | anotherSet
    println(unionSet)
    // so set has all the method like maths concat, intersection and so diff others

  }


  def main(args: Array[String]): Unit = {
    testSeq("Seq Implementation")
    testList("List Implementation")
    testRanges("Ranges Implementation")
    testArray("Array Implementation")
    println(smallBenchMark)
    testSet()
  }
}
