package org.scala.test1.part3FunctionalProg.Practice

import scala.annotation.tailrec

object TuplesMapExercises {
  /**
   * Social network = Map[String, Set[String]] == every person has a set of
   *
   * person -> set of that person's direct friends
   *
   * Daniel -> {Mary, John}
   * Mary   -> {Daniel, Jane}
   * Jane   -> {Mary}
   * John   -> {Daniel}
   *
   * Person -> Set(Friend1,Friend2,Friend3)...
   * Friend relationships are MUTUAL.
   * Daniel is friend with Mary
   * Daniel -> {Mary}
   * Mary   -> {Daniel}
   * this is not the case
   * Daniel -> {Mary}
   * Mary   -> {}
   *
   * - add a person to the network
   * -  Daniel -> {Mary}
   *    Mary   -> {Daniel}
   *    now add
   *    Jane
   *    Daniel -> {Mary}
   *    Mary   -> {Daniel}
   *    Jane   -> {}
   *
   * 2.    * - add friend relationship network(a, b)
   *           friend(Mary, Jane)
   *           Because friendship is mutual, two things must change.
   *           Mary gains Jane:
   *           Mary -> {Daniel, Jane}
   *           Jane gains Mary:
   *           Jane -> {Mary}
   *           Daniel -> {Mary}
   *           Mary   -> {Daniel, Jane}
   *           Jane   -> {Mary}
   *
   *
   * - remove a person from the network
   * -
   * - unfriend
   * - Daniel -> {Mary}
   *   Mary   -> {Daniel, Jane}
   *   Jane   -> {Mary}
   *
   * Now remove
   * Mary
   * Cant delete direct
   * Mary -> {Daniel, Jane}
   * because then Daniel would still contain Mary:
   * Daniel -> {Mary}
   * Jane would still contain Mary:
   * Jane -> {Mary}
   * First:
   * remove Mary as a key
   * Second:
   * remove Mary from everybody else's friend set
   * final result:
   * Daniel -> {}
   * Jane   -> {}
   *
   * - number of friends of a person
   * - Suppose:
   *   Mary -> {Daniel, Jane, Tom}
   *   How many friends does Mary have?
   *   Just count:
   *   {Daniel, Jane, Tom}
   *   So conceptually:
   *   number of friends = size of person's Set
   *
   * - who has the most friends
   * -
   * - Suppose:
   *   Daniel -> {Mary}
   *   Mary   -> {Daniel, Jane, Tom}
   *   Jane   -> {Mary, Tom}
   *   Tom    -> {Mary, Jane}
   *
   * Friend counts:
   * Daniel -> 1
   * Mary   -> 3
   * Jane   -> 2
   * Tom    -> 2
   * So the question is:
   * Which key has the largest friend-set size?
   *
   * - how many people have Ngit sO friends
   * + if there is a social connection between two people (direct or not)
   *
   * Daniel <-> Mary <-> Jane <-> Tom
   */

  def addPerson(network: Map[String, Set[String]], newPerson: String): Map[String, Set[String]] = {
    network + (newPerson -> Set())
  }

  def addFriendRelation(network: Map[String, Set[String]], aPerson: String, bPerson: String): Map[String, Set[String]] = {
    if (!network.contains(aPerson)) throw new IllegalArgumentException(s"sThe person $aPerson is not part of the network")
    else if (!network.contains(bPerson)) throw new IllegalArgumentException(s"sThe person $bPerson is not part of the network")
    else {
      val aPerSet = network(aPerson) + bPerson
      val bPerSet = network(bPerson) + aPerson

      network + (aPerson -> (aPerSet), bPerson -> (bPerSet))
    }
  }


  def removePerson(network: Map[String, Set[String]], person: String): Map[String, Set[String]] = {
    (network - person).map(pair => (pair._1, pair._2 - person))
  }


  def unfriend(network: Map[String, Set[String]], aPerson: String, bPerson: String): Map[String, Set[String]] = {
    if (!network.contains(aPerson)) throw new IllegalArgumentException(s"sThe person $aPerson is not part of the network")
    else {
      val aPersonSet = network(aPerson) - bPerson
      val bPersonSet = network(bPerson) - aPerson

      network + (aPerson -> aPersonSet, bPerson -> bPersonSet)
    }
  }

  def numberOfFriendOfPerson(network: Map[String, Set[String]], aPerson: String): Int = {
    network(aPerson).size
  }

  def mostFriend(network: Map[String, Set[String]]) = {
    if (network.isEmpty) throw new RuntimeException("Network is empty, no-one with most friends")
    else {
      /*
        Example breakdown:
          Map(Bob -> Set(Mary), Mary -> Set(Bob, Jim), Jim -> Set(Mary))

        ("", -1), (Bob, [Mary]) => (Bob, 1)
        (Bob, 1), (Mary, [Bob, Jim]) => (Mary, 2)
        (Mary, 2), (Jim, [Mary]) => (Mary, 2)
        (Mary, 2)
       */
      val best = network.foldLeft(("", -1)) { (currentBest, newAssociation) =>
        // code block
        val currentMostPopularPerson = currentBest._1
        val mostFriendsSoFar = currentBest._2

        val newPerson = newAssociation._1
        val newPersonFriends = newAssociation._2.size

        if (mostFriendsSoFar < newPersonFriends) (newPerson, newPersonFriends)
        else currentBest
      }

      best._1
    }
  }

  // 2
  def nFriends(network: Map[String, Set[String]], person: String): Int =
    if (!network.contains(person)) -1
    else network(person).size

  def nPeopleWithNoFriends(network: Map[String, Set[String]]): Int =
    network.count(pair => pair._2.isEmpty)

  def socialConnection(network: Map[String, Set[String]], a: String, b: String): Boolean = {
    /*
        Example breakdown:
          Map(Bob -> Set(Mary), Mary -> Set(Bob, Jim), Jim -> Set(Mary, Daniel), Daniel -> Set(Jim))

        socialConnection(network, Bob, Jim) =
        search([Mary], [Bob])) =
        true

        socialConnection(network, Bob, Daniel) =
        search([Mary], [Bob]) =
        search([] ++ [Bob, Jim] -- [Bob], [Bob, Mary]) =
        search([Jim], [Bob, Mary]) =
        true
       */
    // Breadth-first search

    @tailrec
    def search(discoveredPeople: Set[String], consideredPeople: Set[String]): Boolean =
      if (discoveredPeople.isEmpty) false
      else {
        val person = discoveredPeople.head
        val personsFriends = network(person)

        if (personsFriends.contains(b)) true
        else search(discoveredPeople - person ++ personsFriends -- consideredPeople, consideredPeople + person)
      }

    if (!network.contains(a) || !network.contains(b)) false
    else search(Set(a), Set(a))
  }

  def main(args: Array[String]): Unit = {
    val empty: Map[String, Set[String]] = Map()
    val network = addPerson(addPerson(empty, "Bob"), "Mary")
    println(network)
    println(addFriendRelation(network, "Bob", "Mary"))
    println(unfriend(addFriendRelation(network, "Bob", "Mary"), "Bob", "Mary"))

    val people = addPerson(addPerson(addPerson(empty, "Bob"), "Mary"), "Jim")
    val simpleNet = addFriendRelation(addFriendRelation(people, "Bob", "Mary"), "Jim", "Mary")
    println(simpleNet)
    println(nFriends(simpleNet, "Mary")) // 2
    println(nFriends(simpleNet, "Bob")) // 1
    println(nFriends(simpleNet, "Daniel")) // -1

    println(mostFriend(simpleNet))

    println(nPeopleWithNoFriends(addPerson(simpleNet, "Daniel")))

    println(socialConnection(simpleNet, "Bob", "Jim")) // true
    println(socialConnection(addFriendRelation(network, "Bob", "Mary"), "Bob", "Mary")) // true
    println(socialConnection(addPerson(simpleNet, "Daniel"), "Bob", "Daniel")) // false
  }
}
