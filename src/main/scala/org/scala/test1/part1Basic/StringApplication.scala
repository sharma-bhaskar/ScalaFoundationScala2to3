package org.scala.test1.part1Basic

object StringApplication {

  val aString = "Hello, I am providing some access to scala 3"

  val aChar: Char = aString.charAt(1)
  val firstWord: String = aString.substring(0,5)
  val splitString: Array[String] = aString.split(" ")
  val upperCaseString: String = aString.toUpperCase()
  val lenghtOfString: Int = aString.length
  val startsWith: Boolean = aString.startsWith("Hello")

  //other string function lib like reverse, string interpoliation and so on

  def main(args: Array[String]): Unit = {
    println(startsWith)
  }
}
