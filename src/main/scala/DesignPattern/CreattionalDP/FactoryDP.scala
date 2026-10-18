package DesignPattern.CreattionalDP

object FactoryDP {

  //What is the Factory Method Pattern?
  // Factory method defines an interface for creating an object but lets subclasses decide which concrete class to intantiate
  //In simple term -- The code that uses an object should not always need to know exactly which concrete class creates it

  //lets imagine a payment system
  // we support two payment method
  // UPI and Credit Card
  // Later we support wallets and net banking
  // Initially our impl might look

  class UPIGw {
    def pay(amount: BigDecimal): Unit = {
      println(s"UPI Payment $amount")
    }
  }

  class CardGW {
    def pay(amount: BigDecimal): Unit = {
      println(s"Card Payment $amount")
    }
  }

  class CheckOutService {
    def checkout(method: String, amount: BigDecimal): Unit = {
      if (method == "UPI") {
        val gw = new UPIGw()
        gw.pay(amount)
      } else if (method == "CARD") {}
      val gw = new CardGW()
      gw.pay(amount)
    }
  }
  //What's wrong with this design?
  //Our CheckoutService is responsible for two things:
  //1. Processing the checkout workflow.
  //2. Deciding which concrete gateway to instantiate.

  //Every time we introduce a new provider, we modify the checkout logic.
  //This introduces coupling between business logic and object creation.
  //Our goal: Separate the common checkout workflow from the decision about which gateway to create.

  //2. First understand simple factory pattern
  // A simple factory centralize object creation in one place

  //step 1 Define common abstraction

  trait PaymentGW_V1 {
    def pay(amount: BigDecimal): Unit
  }

  class UPIGW extends PaymentGW_V1 {
    override def pay(amount: BigDecimal): Unit = println(s"Paid $amount through UPI")
  }

  class CardGWV1 extends PaymentGW_V1 {

    override def pay(amount: BigDecimal): Unit = println(s"Paid $amount through Card")
  }

  //Step 2: Create a factory

  object PaymentGWFactor {
    def create(method: String): PaymentGW_V1 = {
      method match {
        case "UPI" => new UPIGW()
        case "CARD" => new CardGWV1()
        case other => throw new UnsupportedOperationException(s"Unsupported payment method: $other")
      }
    }
  }

  //Step 3: Use the factory
  class CheckoutService {

    def checkout(
                  method: String,
                  amount: BigDecimal
                ): Unit = {
      val gateway = PaymentGWFactor.create(method)
      gateway.pay(amount)
    }
  }
  //Now our CheckoutService doesn't know about UpiGateway or CardGateway.
  //It knows only the PaymentGateway abstraction.

  //This is useful, but it is not the classic Gang of Four Factory Method Pattern

  // Why
  // Adding a new gateway still requires editing the PaymentGatewayFactory match expression
  // Actual factory method pattern introduce a different extension mech

  //3 Factor method - The actual design pattern, The Pattern has four key

  /**
   * Let's build it properly.
   * The pattern has four key participants.
   * Participant	                  Responsibility	                                       Our example
   * Product	                      Defines the object contract	                           PaymentGateway
   * Concrete                       Product	Implements that contract	                     UpiGateway, CardGateway
   * Creator	                      Declares the factory method and shared workflow        CheckoutProcessor
   * Concrete Creator	              Decides which object to create	                       UpiCheckoutProcessor, CardCheckoutProcessor
   *
   * */

  // Step 1: Define the Product
  trait PaymentGW_V2 {
    def pay(amount: BigDecimal): Unit
  }

  //step 2 Define the concrete product

  class UPIGW_V2 extends PaymentGW_V2 {

    override def pay(amount: BigDecimal): Unit = println(s"UPI: $amount")
  }

  class CardGW_V2 extends PaymentGW_V2 {

    override def pay(amount: BigDecimal): Unit = println(s"Card: $amount")
  }

  //Step 3: Define the Creator

  abstract class CheckOutServiceProcessor {
    //factory method
    protected def createGw(): PaymentGW_V2

    //common business workflow
    final def checkout(amount: BigDecimal): Unit = {
      require(amount > 0, "Amount must be positive")

      //Notice the separation:
      //- checkout() contains the common business workflow.
      //- createGateway() is abstract.
      //- The creator does not know which concrete gateway will be created.
      //The concrete subclass provides that decision.

      val gw = createGw()
      gw.pay(amount)
      println("Checkout completed")
    }
  }

  //step 4 - Create Concrete Creator
  class UPICheckoutProcessor extends CheckOutServiceProcessor {

    override protected def createGw(): PaymentGW_V2 = new UPIGW_V2()
  }

  class CardCheckOutProcessor extends CheckOutServiceProcessor {

    override protected def createGw(): PaymentGW_V2 = new CardGW_V2()
  }

  def main(args: Array[String]): Unit = {
    val upi = new UPICheckoutProcessor()
    upi.checkout(500)
    val card = new CardCheckOutProcessor()
    card.checkout(999)

    val processor: CheckOutServiceProcessor = {
      new UPICheckoutProcessor()

      //1. processor.checkout() executes the common method defined in CheckoutProcessor.
      //2. Inside it, createGateway() is called.
      //3. Because the actual instance is UpiCheckoutProcessor, Scala dynamically dispatches to its overridden createGateway().
      //4. That method returns a UpiGateway, referenced through the PaymentGateway trait.
      //5. The common checkout workflow calls gateway.pay().
      //This is where Factory Method connects with the polymorphism and method overriding concepts you've already studied.
      //The creator controls when to create and use the product. The subclass controls what concrete product gets created.
    }

    processor.checkout(BigDecimal(500))
  }

}
