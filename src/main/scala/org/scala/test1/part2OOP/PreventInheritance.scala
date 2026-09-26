package org.scala.test1.part2OOP

object PreventInheritance {

  //some times we dont need to inherit or expose the properties of the class in scala inheritance is very discouraging
  // to achieve that scala has final keyword or prevent the inheritance
  // also in scala 3 they introduce open keyword for such cases like no access modifiers that means can access

  class Person(name: String) {
    final def enjoyLife(): Int = 32
  }
  //using this final keyword not possible to inherit this method, also we can make class level access modifiers to prevent the inheritance

  final class Animal

  // other type is sealed type of inheritance where we declare it but cannot access it outside the file

  sealed class Guitar(nString: Int)

  class ElectrictGuitar(nString: Int) extends Guitar(nString)

  class AcousticGuitar(nString: Int) extends Guitar(nString)
  
  //now third in scala 3 to use open keyword and its good practice
  
  open class ExtensibleGuitar(nString:Int)

  def main(args: Array[String]): Unit = {

  }
}
