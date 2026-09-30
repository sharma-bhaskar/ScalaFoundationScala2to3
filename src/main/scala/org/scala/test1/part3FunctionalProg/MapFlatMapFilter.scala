package org.scala.test1.part3FunctionalProg

object MapFlatMapFilter {

  val aList = List(1, 2, 3)
  val head = aList.head
  val restOfTheList = aList.tail

  //Scala already has the special higher order function
  // like map, flatmap and filter

  //map
  val aInc = aList.map(_ + 1)

  //filter
  val onlyOddNo = aList.filter(_ % 2 == 0)

  //flatmap
  val toPair = (x: Int) => List(x, x + 1)

  val aFlatmap = aList.flatMap(toPair)

  // Exercise - All the possible combinations of all the elements of those lists, in the format
  // "1a - black"
  val numbers = List(1, 2, 3, 4)
  val chars = List('a', 'b', 'c', 'd')
  val colors = List("black", "white", "red")

  /*
      lambda = num => chars.map(char => s"$num$char")
      [1,2,3,4].flatMap(lambda) = ["1a", "1b", "1c", "1d", "2a", "2b", "2c", "2d", ...]
      lambda(1) = chars.map(char => s"1$char") = ["1a", "1b", "1c", "1d"]
      lambda(2) = .. = ["2a", "2b", "2c", "2d"]
      lambda(3) = ..
      lambda(4) = ..
     */
  val posCombination = numbers.flatMap(x => chars.flatMap(y => colors.map(z => s"$x$y-$z")))

  //followingg is such type of code is bit tricky and loose the gap, so scala has for compreshive

  private val combination4: Seq[String] = for {
    number <- numbers
    char <- chars
    colors <- colors
  } yield s"$number$char-$colors" // so this is for structure is expression this is just a compacted version


  //just the even number of this types
  val EvenposCombination = numbers.withFilter(x => x % 2 == 0).flatMap(x => chars.flatMap(y => colors.map(z => s"$x$y-$z")))

  private val evenCombination4: Seq[String] = for {
    number <- numbers if number % 2 == 0
    char <- chars
    colors <- colors
  } yield s"$number$char-$colors"

  //for compreshive using with side effect

  /**
   * Exercises
   * 1. LList supports for comprehensions?
   * 2. A small collection of AT MOST ONE element - Maybe[A]
   *  - map
   *  - flatMap
   *  - filter
   */

  def main(args: Array[String]): Unit = {
    println(posCombination)
    println(combination4)
    for{
      num <- numbers
    } println(num)
    println(EvenposCombination)
    println(evenCombination4)
  }

}
