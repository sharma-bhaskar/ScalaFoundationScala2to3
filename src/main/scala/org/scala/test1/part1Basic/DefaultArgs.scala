package org.scala.test1.part1Basic

import scala.annotation.tailrec

object DefaultArgs {

  //sum of all the number using tail recursion

  @tailrec
  def sumUnitlAllRec(x: Int, acc: Int = 0): Int = {
    if (x <= 0) acc
    else sumUnitlAllRec(x - 1, acc + x)
  }

  // when you use a function most of the time with the same value = default arguments
  def savePicture(dirPath: String, name: String, format: String = "jpg", width: Int = 1920, height: Int = 1080): Unit =
    println("Saving picture in format " + format + " in path " + dirPath)

  def main(args: Array[String]): Unit = {
    // default args are injected
    savePicture("/users/daniel/photos", "myphoto")
    // pass explicit different values for default args
    savePicture("/users/daniel/photos", "myphoto", "png")
    // pass values after the default argument
    savePicture("/users/daniel/photos", "myphoto", width = 800, height = 600)
    // naming arguments allow passing in a different order
    savePicture("/users/daniel/photos", "myphoto", height = 600, width = 800)
  }
}
