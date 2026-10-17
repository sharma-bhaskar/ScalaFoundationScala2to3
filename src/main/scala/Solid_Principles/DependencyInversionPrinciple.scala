package Solid_Principles

object DependencyInversionPrinciple {

  //Definition: High-level business logic should not depend directly on low-level implementation details. Both should depend on abstractions.

  //Wrong Approach
  class RazorPayGW {
    def charge(amount: BigDecimal): String = s"Charged RGW"
  }

  class CheckOutService {
    private val gw = new RazorPayGW()

    def checkOutService(amount: BigDecimal): String = gw.charge(amount)
  }

  //The business service constructs its own payment provider.
  //Tomorrow, suppose we migrate to another gateway, or want to test checkout without sending real payments.
  //The service is tightly coupled to RazorpayGateway

  //  Correct approach: Dependency inversion

  trait PaymentGw {
    def charge(amount: BigDecimal): String
  }

  class RazorPayGateWay extends PaymentGw {

    override def charge(amount: BigDecimal): String = s"RazorPay Gateway"
  }

  class CheckoutService(gw: PaymentGw) {
    def checkOutService(amount: BigDecimal): String = gw.charge(amount)
  }


  //test with fake Payment Gw

  class FakePaymentGW extends PaymentGw {

    override def charge(amount: BigDecimal): String =
      s"I am Fake Payment GW"
  }

  def main(args: Array[String]): Unit = {

    val gw = new RazorPayGateWay()
    val checkOutService = new CheckoutService(gw)

    println(checkOutService.checkOutService(10.20))

  }
}
