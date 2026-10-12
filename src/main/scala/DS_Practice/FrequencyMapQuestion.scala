package DS_Practice


/**
 * Input:
 * [1, 1, 1, 2, 2, 3]
 *
 * Frequencies:
 * 1 -> 3
 * 2 -> 2
 * 3 -> 1
 *
 * Answer:
 * 3 - 1 = 2
 * */
object FrequencyMapQuestion {

  //Given an array , find the difference
  // between the highest frequency and the lowest frequency of any number.
  def frequenceyCal(nums: Array[Int]): Int = {
    val freqMap = nums.groupMapReduce(identity)(_ => 1)(_ + _)

    freqMap.values.max - freqMap.values.min
  }

  //Practice question: Most Frequent Number
  //Given an array of integers, return the number that appears most frequently.
  private def mostFrquentNumber(nums: Array[Int]) = {
    val freqMap = nums.groupMapReduce(identity)(_ => 1)(_ + _)

    val maxFreq = freqMap.values.max

    freqMap.filter { case (_, count) => count == maxFreq }.keys.toArray.sorted
  }

  //Practice question: Least Frequent Number
  //Given an array of integers, return the number that appears the fewest times.
  def leastFrequent(nums: Array[Int]): Array[Int] = {
    val frqMap = nums.groupMapReduce(identity)(_ => 1)(_ + _)
    val minFrq = frqMap.values.min
    frqMap.filter {
      case (i, count) => count == minFrq
    }.keys.toArray.sorted
  }

  //Practice: All Most-Frequent Numbers-- find those have all max frequence
  def allMostFreq(nums: Array[Int]): Array[Int] = {
    val frqMap = nums.groupMapReduce(identity)(_ => 1)(_ + _)
    val maxFrq = frqMap.values.max
    frqMap.filter {
      case (i, count) => count == maxFrq
    }.keys.toArray
  }

  //Practice: Return numbers that appear exactly twice

  def nMostFreq(nums: Array[Int], n: Int): Array[Int] = {
    val frqMap = nums.groupMapReduce(identity)(_ => 1)(_ + _)

    frqMap.filter {
      case (i, count) => count == n
    }.keys.toArray
  }

  def main(args: Array[String]): Unit = {
    println(s"Frequency Max - Min =>  ${frequenceyCal(Array(1, 1, 1, 1, 2, 3, 2, 3))}")
    println(s"Most Frequent =>  " +
      s"${mostFrquentNumber(Array(1, 1, 1, 1, 2, 3, 2, 3, 5, 5, 5)).mkString("Array(", ", ", ")")}")
    println(s"Most Frequent =>  " +
      s"${leastFrequent(Array(1, 1, 1, 1, 0, 2, 3, 2, 3, 5, 5, 5)).mkString("Array(", ", ", ")")}")
    println(s"All Most Frequent =>  " +
      s"${allMostFreq(Array(1, 1, 1, 1, 0, 2, 3, 2, 3, 5, 5, 5)).mkString("Array(", ", ", ")")}")
    println(s"All Most Frequent =>  " +
      s"${nMostFreq(Array(1, 1, 1, 1, 0, 2, 3, 2, 3, 5, 5, 5),2).mkString("Array(", ", ", ")")}")

  }

}
