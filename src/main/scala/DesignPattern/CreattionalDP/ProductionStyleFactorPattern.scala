package DesignPattern.CreattionalDP

object ProductionStyleFactorPattern {

  final case class PaymentRequest(amount: BigDecimal, currency: String)

  final case class PaymentReceipt(transactionId: String, provider: String)

  trait PaymentGW {
    def charge(request: PaymentRequest): Either[String, PaymentReceipt]
  }

  final class UPIGW extends PaymentGW {

    override def charge(request: PaymentRequest): Either[String, PaymentReceipt] = Right(PaymentReceipt("UPI-001", "UPI"))

  }

  final class CardGW extends PaymentGW {

    override def charge(request: PaymentRequest): Either[String, PaymentReceipt] = Right(PaymentReceipt("Card-001", "CARD"))
  }

  // Creator
  abstract class CheckOutProcessor {
    protected def createGW(): PaymentGW

    final def checkout(request: PaymentRequest): Either[String, PaymentReceipt] = {
      if (request.amount <= 0) {
        Left("Invalid Payment")
      } else {
        val gw = createGW()
        gw.charge(request)
      }
    }
  }

  // Concrete Creators

  final class UPICheckoutProcessor extends CheckOutProcessor {

    override protected def createGW(): PaymentGW =
      new UPIGW()
  }

  final class CardCheckoutProcessor extends CheckOutProcessor {

    override protected def createGW(): PaymentGW = new CardGW()
  }

  def main(args: Array[String]): Unit = {
    val processor = new UPICheckoutProcessor()
    val req = PaymentRequest(BigDecimal(500), "INR")

    //This demonstrates a clean Factory Method structure with explicit error handling.
    //It is still an educational simulation.
    // A real implementation would require asynchronous provider calls, authentication, timeouts, retries, idempotency, and secure handling of payment data.
    //Also, in a production system we may avoid constructing a costly gateway client on every checkout.
    // The factory method could return an already configured instance.
    println(processor.checkout(req))
  }
  //7. Why and why not Factory Method?
  //When Factory Method is useful
  //It fits when different creator types share an overall workflow but need to instantiate different products.
  //Examples:
  //- Different exporters create PDF, CSV, or JSON writers.
  //- Different message processors construct Kafka, RabbitMQ, or other publishers.
  //- Different document workflows create specialized document handlers.
  //- Different payment processors instantiate provider-specific gateways.


  //  8. Idiomatic Scala approach
  enum PaymentMethod {
    case UPI, CARD
  }

  trait PaymentGW_V4 {
    def pay(amount: BigDecimal): Unit
  }

  class UPIGateWay extends PaymentGW_V4 {

    override def pay(amount: BigDecimal): Unit = println(s"UPI payment: $amount")

  }

  class CardGateway extends PaymentGW_V4 {

    override def pay(amount: BigDecimal): Unit = println(s"Card payment: $amount")
  }

  object PaymentMethod {
    def apply(method: PaymentMethod): PaymentGW_V4 = {
      method match {
        case PaymentMethod.UPI => new UPIGateWay()
        case PaymentMethod.CARD => new CardGateway()
      }
    }
  }

}