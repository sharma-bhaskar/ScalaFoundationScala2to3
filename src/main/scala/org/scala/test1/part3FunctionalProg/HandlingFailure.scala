package org.scala.test1.part3FunctionalProg

import scala.util.{Failure, Random, Success, Try}

object HandlingFailure {

  //Try:  This is DS where handling the failure is much better way rather thn depend or try catch or throwing null
  // so its better to capture the exception at computation part

  val aTry = Try(309) //this is try of Int similar like other collection
  // try is companion object so we can use without new

  val aFailedTry = Try(throw new RuntimeException)

  // Try has two subtype i.e Success and Failure and that return TRY only

  val successTry = Success(20)
  //  val failureTry : Try[Int] = Failure(throw new RuntimeException("Failure Try "))
  // so we generally dont use Success and Failure Subtype let Try handles this

  // utilities
  val checkSucess = aTry.isSuccess
  val checkFailure = aTry.isFailure

  // another this also has the HOF map, flatmap and filter
  val incMapTry = aTry.map(_ + 1)
  val flatmapTry = aTry.flatMap(mol => Try(s"my life rule $mol"))
  val filterTry = aTry.filter(_ % 2 == 0)

  // Try also elgible for chain
  //  val chain = failureTry.orElse(Try("Success"))

  //this is how we avoid the worst defensive try catch scen
  // Best practive wrap them TRY
  //purely function
  // DESIGN
  def betterUnsafeMethod(): Try[String] = Failure(new RuntimeException("No string for you, buster!"))

  def betterBackupMethod(): Try[String] = Success("Scala")

  val stringLengthPure_v2 = betterUnsafeMethod().map(_.length)
  val aSafeChain = betterUnsafeMethod().orElse(betterBackupMethod()).map(_.length)


  /**
   * Exercise:
   * obtain a connection,
   * then fetch the url,
   * then print the resulting HTML
   */

  val host = "localhost"
  val port = "8081"
  val myDesiredURL = "mindin.com/home"

  class Connection {
    val random = new Random()

    def get(url: String): String = {
      if (random.nextBoolean()) "<html>Success</html>"
      else throw new RuntimeException("Cannot fetch page right now.")
    }

    def getSafe(url: String): Try[String] =
      Try(get(url))
  }

  object HttpService {
    val random = new Random()

    def getConnection(host: String, port: String): Connection =
      if (random.nextBoolean()) new Connection
      else throw new RuntimeException("Cannot access host/port combination.")

    def getConnectionSafe(host: String, port: String): Try[Connection] =
      Try(getConnection(host, port))
  }

  //purely functional

  val obtainConnect = Try(HttpService.getConnection(host, port))
  val fetchUrl = obtainConnect.flatMap(con => Try(con.get(myDesiredURL)))
  val finalResult = fetchUrl.fold(e => s"<html>${e.getMessage}</html>", s => s)

  //for compresnshiv

  val finalCon = for {
    conn <- HttpService.getConnectionSafe(host, port)
    fetchUrl <- conn.getSafe(myDesiredURL)
  } yield fetchUrl

  val result = finalCon.fold(e => s"<html>${e.getMessage}</html>", s => s)

  def main(args: Array[String]): Unit = {
    println(aTry)
    println(aFailedTry) // so this is safe try
    println(stringLengthPure_v2)
    println(aSafeChain)
    println(finalResult)

  }
}

