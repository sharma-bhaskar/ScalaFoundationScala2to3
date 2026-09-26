package org.scala.test1.part2OOP

object CaseClasses {

  //case class in scala is product type of ADT this ADT is Algebric data types and its has
  // some properties where it will automatically enhance our case classes
  // like it provide inbuilt properties like toString, equals, hashcode
  // utility method copy and of other like productiterator and so on

  //declare a person case class
  case class Person(name: String, age: Int)

  val daniel = new Person("Daniel", 99)
  val danielAge = daniel.age

  // as its automatically give as special method apply so we can directly use apply method in this
  val danielV2 = Person("Daniel", 99)
  //equal check
  private val isSameDaniel = daniel == danielV2 // so equal check the sameness not the reference if we check eq ref thn it will give false
  private val isSameDanielRef = daniel eq danielV2

  //case classes is lightweight data structure and hold the data and send it over network or serialize purpose

  val danielYoung = danielV2.copy(age = 23) //copy method is use to update the case class field 

  //CCs class have companion object 

  //if we don't have any argument we can use the case object like this 

  case object UK {
    def name: String = "The UK of GB & NI"
  }
  //main properties of case object with no arg the use object
  

  def main(args: Array[String]): Unit = {
    println(isSameDaniel)
    println(isSameDanielRef)

  }

}
