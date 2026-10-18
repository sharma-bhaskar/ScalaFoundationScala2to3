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

}
