package DS_Practice

object PairLookup {

  //Core problem: Two Sum existence Two Sum / pair lookup with Set or HashMap

  /*
      nums = Array(2, 7, 11, 15)
      target = 9
      Return:
          true
  *
  *
  * */

  def hasPairWithSum(nums: Array[Int], target: Int): Boolean = {
    nums.foldLeft(Set.empty[Int], false) {
      case ((seen, found), value) =>
        if (found) (seen, true)
        else
          val needed = target - value
          if (seen.contains(needed)) (seen, true)
          else (seen + value, false)
    }._2
  }

  //Practice Next variation: return the actual pair, not just true/false.

  def returnPairOfSum(nums: Array[Int], target: Int): Option[(Int, Int)] = {
    nums.foldLeft((Set.empty[Int], Option.empty[(Int, Int)])) {
      case ((seen, result), value) =>
        result match {
          case some@Some(_) =>
            (seen, some)
          case None =>
            val needed = target - value

            if seen.contains(needed) then
              (seen, Some((needed, value)))
            else
              (seen + value, None)
        }
    }._2
  }

  //Next variation: return the indices of the pair, not the values.
  def returnIndexOfPair(nums:Array[Int],target:Int): Option[(Int, Int)] = {
    nums.zipWithIndex.foldLeft((Map.empty[Int,Int],Option.empty[(Int,Int)])) {
      case ((seen,result),(value,index)) =>
        result match {
          case some @ Some(_) => (seen,some)
          case None =>
            val needed = target - value

            seen.get(needed) match {
              case Some(previousIndex) => (seen,Some(previousIndex,index))
              case None => (seen + (value -> index),None)
            }
        }
    }._2
  }

  //Next Variation: count how many pairs sum to target.

  private def countPair(nums:Array[Int], target:Int): Int = {
    nums.foldLeft(Map.empty[Int,Int],0) {
      case ((seen,countPairs),value) =>

        val needed = target - value

        val matches =
          seen.getOrElse(needed, 0)

        val updatedSeen =
          seen.updated(
            value,
            seen.getOrElse(value, 0) + 1
          )

        (updatedSeen, countPairs + matches)
    }._2
  }


  def main(args: Array[String]): Unit = {
    println(s"Two Sum existence Two Sum => " +
      s"${hasPairWithSum(Array(2, 7, 11, 15), 9)}")
    println(s"Next variation: return the actual pair, not just true/false. => " +
      s"${returnPairOfSum(Array(2, 7, 11, 15), 9)}")
    println(s"Next variation: return the indices of the pair, not the values. =>" +
      s" ${returnIndexOfPair(Array(2, 7, 11, 15), 9)}")
    println(s"Next variation: return the indices of the pair, not the values. =>" +
      s" ${countPair(Array(2, 7, 11, 15), 9)}")

  }

}
