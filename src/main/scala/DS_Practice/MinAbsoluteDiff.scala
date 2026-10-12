package DS_Practice


object MinAbsoluteDiff {

  //Problem: Minimum Absolute Difference.
  //Given an array, find the minimum absolute difference between any two elements

  /**
   * Example:
   * Array(4, 2, 1, 3)
   *
   * Possible differences include:
   * |4 - 2| = 2
   * |4 - 1| = 3
   * |4 - 3| = 1
   * |2 - 1| = 1
   * |2 - 3| = 1
   * |1 - 3| = 2
   *
   * Answer:
   * 1
   * */

  private def minAbsoluteDiff(nums: Array[Int]): Int = {
    nums.sorted.sliding(2).map {
      case Array(a, b) => b - a
    }.min
  }

  //Practice: Return all pairs with the minimum absolute difference
  private def allMinAbsPair(nums: Array[Int]): Array[(Int, Int)] = {
    val pair = nums.sorted.sliding(2).map { case Array(a, b) => ((a, b), b - a) }.toArray
    val minDiff = pair.map(_._2).min

    pair.filter {
      case (tuple, diff) => diff == minDiff
    }.map(_._1)
  }

  //Practice: Count how many pairs have the minimum absolute difference

  private def countPairMin(nums: Array[Int]): Int = {
    val pair = nums.sorted.sliding(2).map {
      case Array(a, b) => b - a
    }.toArray

    val minDif = pair.min

    pair.count(_ == minDif)
  }

  //Practice: Variation: Check whether any pair has difference exactly k
  // Example:
  //nums = Array(1, 5, 3, 4, 2)
  //k = 2
  //
  //Expected:
  //true
  private def kDiffPair(nums: Array[Int], k: Int): Boolean = {
    val uniquePair = nums.toSet
    uniquePair.exists(value => uniquePair.contains(value + k))

  }

  def main(args: Array[String]): Unit = {
    println(s"Min Absolute Diff =>  ${minAbsoluteDiff(Array(4, 2, 1, 3))}")
    println(s"all pairs with the minimum absolute difference  =>  " +
      s"${allMinAbsPair(Array(4, 2, 1, 3)).mkString("Array(", ", ", ")")}")
    println(s"Count how many pairs have the minimum absolute difference => " +
      s" ${countPairMin(Array(4, 2, 1, 3))}")
    println(s"Variation: Check whether any pair has difference exactly k =>  " +
      s"${kDiffPair(Array(4, 2, 1, 3), 0)}")


  }

}
