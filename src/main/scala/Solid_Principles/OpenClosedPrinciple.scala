package Solid_Principles

object OpenClosedPrinciple {

  //O — Open/Closed Principle (OCP)
  //Definition: Software entities should be open for extension but closed for modification.

  class PaymentService {
    def pay(method: String): Unit = {
      if (method == "UPI") {
        println("UPI")
      } else if (method == "Card") {
        println("Card")
      } else if (method == "wallet") {
        println("Wallet")
      }
    }
  } //suppose we need to introduce net banking and then we need to modify paymentService

  //Correct approach: Use polymorphism
  trait PaymentMethod {
    def pay(amount: BigDecimal): Unit
  }

  class UPIPayment extends PaymentMethod {

    override def pay(amount: BigDecimal): Unit = println("UPI Payment Method")
  }

  class CardPayment extends PaymentMethod {

    override def pay(amount: BigDecimal): Unit = println("Card Payment ")
  }

  class PaymentServiceImpl {
    def checkout(paymentMethod: PaymentMethod, amount: BigDecimal): Unit =
      paymentMethod.pay(amount)
  }

  //Now suppose you want to add net banking.
  class NetBanking extends PaymentMethod {

    override def pay(amount: BigDecimal): Unit = println("NetBanking Method")
  }
  //Important distinction
  //OCP does not mean you must never edit an existing class.
  //You can still edit code to fix bugs, change requirements, or evolve an API.
  //It means identifying things likely to vary, such as payment providers, and designing a stable extension point.
  //In Scala, OCP can be implemented with traits, higher-order functions, and type classes. Inheritance is only one option.
  
  def main(args: Array[String]): Unit = {

  }
}
