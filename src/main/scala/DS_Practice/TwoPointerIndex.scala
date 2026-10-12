package DS_Practice


import scala.annotation.tailrec

object TwoPointerIndex {

  /*
  Two Pointers: Sorted Squares
  Given a sorted array:
  Array(-4, -1, 0, 3, 10)

Return the squares in sorted order:
Array(0, 1, 9, 16, 100)
  *
  * */
  def twoPointerIndex(nums: Array[Int]): List[Int] = {
    @tailrec
    def loop(left: Int, right: Int, result: List[Int]): List[Int] = {
      if (left > right) {
        result
      }
      else {
        val leftSq = nums(left) * nums(left)
        val rightSq = nums(right) * nums(right)

        if (leftSq > rightSq) loop(left + 1, right, leftSq :: result)
        else loop(left, right - 1, rightSq :: result)
      }
    }

    loop(0, nums.length - 1, Nil)
  }

  //Practice = Pair sum in a sorted array
  //Given a sorted array and a target, return whether any two numbers add up to the target.

  def twoNumberTarget(nums: Array[Int], target: Int): Boolean = {
    @tailrec
    def loop(left: Int, right: Int): Boolean = {
      if (left >= right) false
      else {
        val sum = nums(left) + nums(right)
        if (sum == target) true
        else if (sum < target) {
          loop(left + 1, right)
        } else {
          loop(left, right - 1)
        }
      }
    }

    loop(0, nums.length - 1)
  }

  //Practice : return the actual pair from a sorted array, not just true/false

  def actualPari(nums: Array[Int], target: Int): Option[(Int, Int)] = {
    @tailrec
    def loop(left: Int, right: Int): Option[(Int, Int)] = {
      if (left >= right) None
      else {
        val sum = nums(left) + nums(right)
        if (sum == target) Some((nums(left), nums(right)))
        else if (sum < target) {
          loop(left + 1, right)
        } else {
          loop(left, right - 1)
        }
      }
    }

    loop(0, nums.length - 1)
  }

  //Practice : return the index pair from a sorted array, not just true/false
  def actualIndexPair(nums: Array[Int], target: Int): Option[(Int, Int)] = {
    @tailrec
    def loop(left: Int, right: Int): Option[(Int, Int)] = {
      if (left >= right) None
      else {
        val sum = nums(left) + nums(right)
        if (sum == target) Some((left, right))
        else if (sum < target) {
          loop(left + 1, right)
        } else {
          loop(left, right - 1)
        }
      }
    }

    loop(0, nums.length - 1)
  }

  def main(args: Array[String]): Unit = {
    println(s"Two Pointers: Sorted Squares=> " +
      s" ${twoPointerIndex(Array(-4, -1, 0, 3, 10))}")
    println(s"Two Pointers: Sorted Squares=> " +
      s" ${twoNumberTarget(Array(-4, -1, 0, 3, 10), 8)}")
    println(s"Return the actual pair of Sum from a sorted array, not just true/false=> " +
      s" ${actualPari(Array(-4, -1, 0, 3, 10), 13)}")
    println(s"Practice : return the index pair from a sorted array, not just true/false => " +
      s" ${actualIndexPair(Array(-4, -1, 0, 3, 10), 13)}")

  }

}
