package org.scala.test1.part2OOP

object EnumPractice {

  //Scala 3 enum has good support of data types where it hold the sealed values traits

  enum Permission {
    case READ, WRITE, EXECUTE, NONE
    // we can also create method and field in enum

    def openDocument(): Unit = {
      if (this == READ) println("have permission to read the file")
      else println("Dont have permission to read the file")
    }
  }

  val somePermissions: Permission = Permission.READ

  //enum can have constructor arguments as well

  enum PermissionCon(bits: Int) {
    case READ extends PermissionCon(2)
    case WRITE extends PermissionCon(4)
    case EXECUTE extends PermissionCon(6)
    case NONE extends PermissionCon(0)
  }

  //enum have the companion object

  object PermissionCon {
    def fromBits(bits:Int): PermissionCon = PermissionCon.NONE
  }

  //standard API in enum
  val standaOrdinal: Int = somePermissions.ordinal
  val allPermissions: Array[PermissionCon] = PermissionCon.values // array of all possible values of the enum
  val readPermission: Permission = Permission.valueOf("READ") // Permissions.READ


  def main(args: Array[String]): Unit = {
    println(standaOrdinal)
    println(allPermissions.mkString(","))

  }
}
