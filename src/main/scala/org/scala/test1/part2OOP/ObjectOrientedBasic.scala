package org.scala.test1.part2OOP

object ObjectOrientedBasic {

  //this class person has contructor arguments, and can create insitation type of person
  class Person(val name: String, age: Int) {
    val allCaps = name.toUpperCase()

    //methods
    def greet(name: String): Unit = {
      println(s"Hello from ${this.name} to $name")
    }

    //method overloading
    def greet(): String = {
      s"Hello, from $name"
    }
    //define aux constructor

    def this(name: String) = {
      this(name, 0)
    }

    def this() = {
      this("Jane Doe")
    }

    //also we can set the parameter or default value in the declaration of class only like class Person(name:String = "John")..

  }

  val aPerson = new Person("John", 25)

  //so we classes we have field, method, constructor and aux contructor as well and also class can have the body
  val john: String = aPerson.name

  def main(args: Array[String]): Unit = {
    println(john)
    val charlesDickens = new Writer("Charles", "Dickens", 1812)
    val charlesDickensImpostor = new Writer("Charles", "Dickens", 2021)

    val novel = new Novel("Great Expectations", 1861, charlesDickens)
    val newEdition = novel.copy(1871)

    println(charlesDickens.fullName)
    println(novel.authorAge)
    println(novel.isWrittenBy(charlesDickensImpostor)) // false
    println(novel.isWrittenBy(charlesDickens)) // true
    println(newEdition.authorAge)


    val counter = new Counter()
    counter.print() // 0
    counter.incr().print() // 1
    counter.incr() // always returns new instances
    counter.print() // 0

    counter.incr(10).print() // 10
    counter.incr(20000).print() // 20000


  }
}

/**
 * Exercise: imagine we're creating a backend for a book publishing house.
 * Create a Novel and a Writer class.
 *
 * Writer: first name, surname, year
 *- method fullname
 *
 * Novel: name, year of release, author
 *- authorAge
 *- isWrittenBy(author)
 *- copy (new year of release) = new instance of Novel
 */

class Writer(val firstName: String, val lastName: String, val year: Int) {
  def fullName = s"$firstName $lastName"
}

class Novel(name: String, yor: Int, author: Writer) {
  def authorAge: Int = yor - author.year

  def isWrittenBy(author: Writer): Boolean = {
    this.author == author
  }

  def copy(newYear: Int) = new Novel(name, newYear, author)
}


/**
 * Exercise #2: an immutable counter class
 * - constructed with an initial count
 * - increment/decrement => NEW instance of counter
 * - increment(n)/decrement(n) => NEW instance of counter
 * - print()
 *
 * Benefits:
 * + well in distributed environments
 * + easier to read and understand code
 */


class Counter(count: Int = 0) {

  def incr(): Counter = {
    new Counter(count + 1)
  }

  def dec(): Counter = {
    if(count == 0) this
    else new Counter(count - 1)
  }

  def incr(n: Int): Counter = {
    if (n <= 0) this
    else incr().incr(n - 1)
  }

  def dec(n: Int): Counter = {
    if (n <= 0) this
    else dec().dec(n - 1)
  }

  def print(): Unit =
    println(s"Current count: $count")

}
