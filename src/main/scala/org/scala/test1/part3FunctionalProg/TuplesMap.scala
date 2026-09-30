package org.scala.test1.part3FunctionalProg

object TuplesMap {

  //Tuples and Map
  // Tuples = finite order list with different data types like i can have string , int, double and so on
  //define a tuples

  def testTuples(string: String) = {

    println(s"Tuples ==> $string")
    val tuple_1: (Int, String) = (123, "Bhaskar") // if you see the infer this is Int with String
    // we can have tuple size and this is group of values

    val firstElement = tuple_1._1
    println(firstElement)

    val newTuples = tuple_1.copy(_1 = 35)
    println(s"$newTuples")

    //tuples consider a associativity
    // so tuples of element is this is right associativity
    val aTuples_V2 = 2 -> "jnkjn" //also we can define this with different types that is map
  }

  def testMap(string: String) = {
    val aMap = Map()
    // so map is data structure that hold the key and values this is implementation of hashmap or using hashset basically
    // internal implementation of hash

    val phonebook = Map("Jim" -> 345, "Daniel" -> 455, "Jane" -> 454).withDefaultValue(-1)

    //map DS has bunch of API and similar we can check if the key is present in the map of not like below example

    val maryPhonebook = phonebook("Mary")
    println(maryPhonebook) //so mary key is not present in the map and it will break our functionality so to tackle this situation we need to use
    // with default values so this will print -1 as it dont have the key in this if dont use default value thn it will throw exception
    println(phonebook)

    //search of an key in map has so many ways
    // adding a pair in the map

    val mary = "Mary" -> 989

    val newPhoneBook = phonebook + mary

    // remove a pair from the map

    val newPhonebookMinusDaneil = newPhoneBook - "Daniel"
    println(newPhonebookMinusDaneil)

    // map, flatMap, filter
    // Map("Jim" -> 123, "jiM" -> 999) => Map("JIM" -> ????)
    val aProcessedPhonebook = phonebook.map(pair => (pair._1.toUpperCase(), pair._2))


    //we can turn linear collection in the map using the tomap functionality

    //common functionality on filters is

    val filterKey = newPhonebookMinusDaneil.view.filterKeys(_.startsWith("J")).toMap

    // common usecase of key should not change only value should change

    //mapping values

    val prefixNum = newPhonebookMinusDaneil.view.mapValues(no => s"02455-$no")
    println(prefixNum)

    //now create map using list but with group j is in the same key or so on
    val list = List("Jim", "Jack", "map", "Map", "Bhask", "Vsa")

    val mapGroup = list.groupBy(f => f.charAt(0))
    println(mapGroup)
  }


  def main(args: Array[String]): Unit = {
    testTuples("Tuples Examples")
    testMap("Map examples with APIS")

  }
}
