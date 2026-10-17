package Solid_Principles

object LiskovSubstitutionPrinciple {

  //Definition: If B is a subtype of A, we should be able to use B wherever A is expected without breaking the contract of A.
  //Mathematically, if:
  //\[
  //B <: A
  //\]
  //
  //then an instance of B must preserve the behavioral guarantees expected of A.

  //Classic example: Bird and Penguin
  trait Bird {
    def fly(): Unit
  }

  class Sparrow extends Bird {

    override def fly(): Unit = println("I can fly")
  }

  class Penguin extends Bird {

    override def fly(): Unit = throw new UnsupportedOperationException("Penguin can't fly")
  }

  def makeBirdFly(bird: Bird): Unit = {
    bird.fly()
  }

  makeBirdFly(new Sparrow)
  makeBirdFly(new Penguin)

  // This design say every Bird can fly
  // But a penguin cannot fulfill that contract

  //Correct approach: Model capabilities correctly

  trait BirdV2 {
    def eat(): Unit
  }

  trait FlyingBird extends BirdV2 {
    def fly(): Unit
  }

  class SparrowV2 extends FlyingBird {

    override def fly(): Unit = println("I can fly")

    override def eat(): Unit = println("I can eat")
  }

  class PenguinV2 extends BirdV2 {

    override def eat(): Unit = println("I can only eat ")
  }

  def makeBirdFly(bird: FlyingBird): Unit = {
    bird.fly()
  }

  //  A more practical LSP example
  //  Imagine a discount policy
  //.

  trait Discount {
    def apply(price: BigDecimal): BigDecimal
  }

  class TenPercentDiscount extends Discount {

    override def apply(price: BigDecimal): BigDecimal = price * BigDecimal(0.90)
  }

  // Violates the contract!
  class HiddenFee extends Discount {
    override def apply(price: BigDecimal): BigDecimal =
      price + 50
  }

  //HiddenFee compiles, but it's not a valid discount. A caller expecting the price never to increase would be broken.
  //This teaches us that LSP is about behavioral contracts, not simply matching method signatures.
  //The deeper LSP rules are:
  //- A subtype must not demand stronger preconditions than its parent.
  //- A subtype must not provide weaker postconditions.
  //- A subtype must preserve the parent's invariants.

  def main(args: Array[String]): Unit = {

  }
}
