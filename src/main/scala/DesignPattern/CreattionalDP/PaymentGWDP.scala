package DesignPattern.CreattionalDP

//Problem: Payment Provider Integration
//Intermediate
//
//You're building an e-commerce payment platform supporting two providers: Razorpay and Stripe.
//Every provider supports two related operations:
//- Charging a customer.
//- Refunding a payment.
//Your goal is to ensure the checkout application obtains both services from the selected provider family without knowing their concrete classes.
//Requirements
//1. Create a PaymentGateway trait with charge(amount: BigDecimal): Unit.
//2. Create a RefundService trait with refund(transactionId: String): Unit.
//3. Create Razorpay and Stripe implementations for both interfaces.
//4. Create a PaymentFactory with createGateway() and createRefundService().
//5. Implement RazorpayFactory and StripeFactory.
//6. Create CheckoutApplication that receives PaymentFactory through its constructor.

trait PaymentGateway {
  def charge(amount: BigDecimal): Unit
}

trait RefundService {
  def refund(transactionId: String): Unit
}

class RazorpayGW extends PaymentGateway {

  override def charge(amount: BigDecimal): Unit = println(s"Razorpay GW -- $amount deducted")
}

class RazorPayRefund extends RefundService {

  override def refund(transactionId: String): Unit = println(s"Razorpay Refund Process $transactionId")
}

class StripepayGW extends PaymentGateway {

  override def charge(amount: BigDecimal): Unit = println(s"Stripe GW -- $amount deducted")
}

class StripePayRefund extends RefundService {

  override def refund(transactionId: String): Unit = println(s"Stripe Refund Process $transactionId")
}

trait PaymentFactoryGW {
  def createGw(): PaymentGateway

  def refundService(): RefundService
}

class RazorPay extends PaymentFactoryGW {

  override def createGw(): PaymentGateway = new RazorpayGW()

  override def refundService(): RefundService = new RazorPayRefund()
}

class Stripe extends PaymentFactoryGW {

  override def createGw(): PaymentGateway = new StripepayGW()

  override def refundService(): RefundService = new StripePayRefund()
}

class CheckOutApp(factory: PaymentFactoryGW) {
  private val gw = factory.createGw()
  private val refundSe = factory.refundService()

  def checkOut(amount: BigDecimal): Unit = gw.charge(amount)

  def refund(transacationId: String): Unit = refundSe.refund(transacationId)
}


object PaymentGWDP {

  def main(args: Array[String]): Unit = {
    val gw = new CheckOutApp(new RazorPay())
    gw.checkOut(900)
    gw.refund("X001")

    val gwStripe = new CheckOutApp(new Stripe())
    gwStripe.checkOut(900)
    gwStripe.refund("X001")

  }
}
