package org.scala.test1.part3FunctionalProg

import scala.util.Random

object OptionsExample {

  //Option == "Collection with at most one values

  val aOption: Option[Int] = Option(43) //this is option of int
  val aEmpty = Option.empty

  // Option uses to work with safe API or better practice to work
  // in jvm lang null pointer is headache and it has defensive code to check

  def unsafeMethod(): String = null

  //defensive style
  val stringLengt = Option(unsafeMethod()) //this will give me null pointer exception and to handle this we need to either use exception or some if or else

  // in scala we have options to work with safe api like below we can define

  // so working from defensive style is unsafe to protect this in this is identical example of defensive to potective apis

  val stringLengthOption: Option[Int] = Option(unsafeMethod()).map(_.length) // so this will give me Option

  // And So subtype of Option is Some and None
  // this is case class of Option

  // some ways to define some and none

  val anEmptyOption_v2 = Some(4)
  val anEmptyOption_v3 = None

  // this option support bunch of API's
  val isEmpty = anEmptyOption_v2.isEmpty
  val innerValue = anEmptyOption_v2.getOrElse(8)

  // to extract value from option have some api like get head and all but this unsafe you will get exception

  // hof in options
  //Some API These are useful methods that exist for both scala.Some and None.
  //isDefined — True if not empty
  //isEmpty — True if empty
  //nonEmpty — True if not empty
  //orElse — Evaluate and return alternate optional value if empty
  //getOrElse — Evaluate and return alternate value if empty
  //get — Return value, throw exception if empty
  //fold — Apply function on optional value, return default if empty
  //map — Apply a function on the optional value
  //flatMap — Same as map but function must return an optional value
  //foreach — Apply a procedure on option value
  //collect — Apply partial pattern match on optional value
  //filter — An optional value satisfies predicate
  //filterNot — An optional value doesn't satisfy predicate
  //exists — Apply predicate on optional value, or false if empty
  //forall — Apply predicate on optional value, or true if empty
  //contains — Checks if value equals optional value, or false if empty
  //zip — Combine two optional values to make a paired optional value
  //unzip — Split an optional pair to two optional values
  //unzip3 — Split an optional triple to three optional values
  //toList — Unary list of optional value, otherwise the empty list

  //eg
  val incrementalOption = anEmptyOption_v2.map(_ + 1)
  val optionFilter = anEmptyOption_v2.filter(_ % 2 == 0)
  val flatmapOption = anEmptyOption_v2.flatMap(value => Option(value * 10))

  //chain option //orElse — Evaluate and return alternate optional value if empty

  val anotherOption = Option(24)

  val aChainOption = anEmptyOption_v2 orElse anotherOption

  //  Interacting
  //  with code that can occasionally
  //  return null can be safely wrapped in scala.Option to become None and scala
  //.Some otherwise
  // this is idel case of production when we dont have value and still processing data
  val abc = new java.util.HashMap[Int, String]
  abc.put(1, "A")
  val bMaybe = Option(abc.get(2))
  bMaybe match {
    case Some(b) =>
      println(s"Found $b")
    case None =>
      println("Not found")
  }

  /**
   * Exercise:
   * Get the host and port from the config map,
   * try to open a connection,
   * print "Conn successful"
   * or "Conn failed"
   */

  val netWorkMap = Map("host" -> 192, "port" -> 8080)

  class Connection {
    def connect(): String = "Connection successful"
  }

  object Connection {

    val random = new Random()

    def apply(host: Int, port: Int): Option[Connection] =
      if (random.nextBoolean()) Some(new Connection()) else None
  }

  def connectionCheck() = {
    val host = netWorkMap.get("host")
    val port = netWorkMap.get("port")

    val connection = host.flatMap(h => port.flatMap(p => Connection(h, p)))
    val connect = connection.map(_.connect())

  }

  // for compresh

  val connectSuc = for {
    host <- netWorkMap.get("host")
    port <- netWorkMap.get("port")
    connectionX <- Connection(host,port)
  } yield connectionX.connect()

  def main(args: Array[String]): Unit = {
//    println(bMaybe)
//    println(connect)
    println(connectSuc)

  }
}
