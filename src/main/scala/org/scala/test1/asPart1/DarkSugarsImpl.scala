package org.scala.test1.asPart1

import scala.util.Try

object DarkSugarsImpl {

  // in scala we have a lot of dark sugars for method and arguments such as infix with numberic type and class type

  // 1 - sugar for methods with one argument

  def singleArgument(arg: Int): Int = arg + 1

  // we can all single method argument like this below
  val aMethodCall: Int = singleArgument({
    42
  })

  // other way to call a method below
  val aMethodCall_V2: Int = singleArgument {
    33
  }

  // these are the example of single method type like TRY, Future and so on
  // and these single method type also have   // with hofs

  val aTryInstance = Try {
    throw new RuntimeException
  }

  val aIncList: Seq[Int] = List(1, 2, 3).map { x => x + 1 }

  // 2 - single abstract method pattern (since Scala 2.12)

  trait Action {
    // can also have other implmented fields/methods here
    def act(x: Int): Int
  }

  val anAction = new Action {
    override def act(x: Int): Int = x + 1
  }

  val anotherAction: Action = (x: Int) => x + 1 // this is lambda type implementation of abstract method
  //example in Runnable

  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("Hello I am runnable")
  })

  //also can write
  val aThread_v2 = new Thread(() => println("Hello, I am new version"))

  // 3 - methods ending in a : are RIGHT-ASSOCIATIVE

  val aList = List(1, 2, 3)
  val aPrependList = 0 :: aList // also we can write like this // aList.::(0)

  val aBigList = 0 :: 1 :: 2 :: 3 :: aList // // List(3,4).::(2).::(1).::(0) this is method associative

  //eg
  class MyStream[T] {
    infix def -->:(value: T): MyStream[T] = this // impl not important
  }

  val myStream = 1 -->: 2 -->: 3 -->: 4 -->: new MyStream[Int]

  // 4 Multi word identifiers

  class Talker(name: String) {
    infix def `and then said`(gossip: String): Unit = println(s"$name said $gossip")
  }

  val xyzTalking = new Talker("xyzTalking")

  val responseTalking = xyzTalking `and then said` ("I love gossip")

  //example of this is HTTP connection

  object `Content-Type` {
    val `application/json` = "application/json"
  }

  // 5 - infix types

  import scala.annotation.targetName

//  @targetName("Arrow") // for more readable bytecode + Java interop
//  infix class -->[A, B]

//  val compositeType: Int --> String = new-->[Int, String]

  // 6 - update()
  val anArray = Array(1, 2, 3, 4)
  anArray.update(2, 45)
  anArray(2) = 45 // same

  // 7 - mutable fields
  class Mutable {
    private var internalMember: Int = 0

    def member = internalMember // "getter"

    def member_=(value: Int): Unit =
      internalMember = value // "setter"
  }

  val aMutableContainer = new Mutable
  aMutableContainer.member = 42 // aMutableContainer.member_=(42)

  // 8 - variable arguments (varargs)
  def methodWithVarargs(args: Int*) = {
    // return the number of arguments supplied
    args.length
  }

  val callWithZeroArgs = methodWithVarargs()
  val callWithOneArgs = methodWithVarargs(78)
  val callWithTwoArgs = methodWithVarargs(12, 34)

  val aCollection = List(1, 2, 3, 4)
  val callWithDynamicArgs = methodWithVarargs(aCollection *)


  def main(args: Array[String]): Unit = {
    println(responseTalking)

  }

}
