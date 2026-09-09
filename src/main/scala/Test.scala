import scala.annotation.tailrec

/**
 * Given a sorted array nums and a target value, return the starting and ending position of the target.
 * If the target is not found, return [-1, -1].
 *  Example
 * Input: nums = [5,7,7,8,8,10], target = 8
 * Output: [3,4]
 *
 *
 * Binary Search
 *
 * left: Int
 * right: Int
 * mid : Int
 * result : Int
 * [5, 7, 7, 8, 8, 10]
 *
 * */

object Test extends App {

  def searchValues(nums: Array[Int], target: Int): Array[Int] = {

    @tailrec
    def firstValues(left: Int, right: Int, ans: Int): Int = {
      if (left > right) ans
      else {
        val mid = left + (right - left) / 2
        if (nums(mid) == target) {
          firstValues(left, mid - 1, mid)
        } else if (nums(mid) < target) {
          firstValues(mid + 1, right, ans)
        } else {
          firstValues(left, mid - 1, ans)
        }
      }
    }

    @tailrec
    def lastValue(left: Int, right: Int, ans: Int): Int = {
      if (left > right) ans
      else {
        val mid = left + (right - left) / 2

        if (nums(mid) == target) {
          lastValue(mid + 1, right, mid)
        } else if (nums(mid) < target) {
          lastValue(mid + 1, right, ans)
        } else {
          lastValue(left, mid - 1, ans)
        }
      }

    }

    Array(
      firstValues(0, nums.length - 1, -1),
      lastValue(0, nums.length - 1, -1)
    )

  }

  val nums = Array(5, 7, 7, 8, 8, 10)
  val target = 9

  println(searchValues(nums, target).mkString)


}
