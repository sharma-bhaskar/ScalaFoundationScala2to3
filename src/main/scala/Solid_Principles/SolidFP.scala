package Solid_Principles


class SolidFP {

  //Unlike Java, Scala gives us both object-oriented and functional programming tools.
  //You don't need a trait every time you want an abstraction.
  //For example, consider Dependency Inversion using only functions.

  type Charge = BigDecimal => Either[String, String]

  class CheckOutService(charge: Charge) {
    def checkOutService(amount: BigDecimal): Either[String, String] = charge(amount)
  }

  val fakeCharge: Charge =
    amount => Right(s"Paid $amount")

  val service = new CheckOutService(fakeCharge)

  println(service.checkOutService(BigDecimal(500)))

  //In functional Scala, SOLID often maps naturally to these ideas:
  //SOLID	Functional Scala interpretation
  //SRP	Small, cohesive pure functions and modules
  //OCP	Higher-order functions, composition, type classes
  //LSP	Preserve contracts and algebraic laws
  //ISP	Small function contracts and focused capabilities
  //DIP	Pass functions or abstract effectful services as dependencies

  def main(args: Array[String]): Unit = {

  }
}
