package org.scala.test1.part1

object ValuesAndTypes {

  /**
   * This is values and types in scala generally refer what we have in scala how to define values and types as well
   * */
  def main(args: Array[String]): Unit = {

    // variable declaration using this val consider is not assigning similar like const and final in other lang

    val testOfLife: Int = 42 // so in this we have define return type Int but in scala it can be automatic

    val testOfLife2 = 42 //this can be define is type inference so scala treat this as Int

    //Other types
    val aInt: Int = 23 // 4 Bytes
    val aBoolean: Boolean = false
    val aChar: Char = 'a' // in char and string scala has single and double quote notation using single quote we are defining char and double string
    val aString: String = "abc"
    val aShort: Short = 2453 // 2 bytes
    val aLong: Long = 98987987988798L // 8 bytes
    val aFloat: Float = 2.5f // 4 bytes
    val aDouble: Double = 3.14 // 8 bytes
    
  }

}
