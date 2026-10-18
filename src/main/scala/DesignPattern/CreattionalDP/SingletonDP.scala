package DesignPattern.CreattionalDP

import java.util.concurrent.atomic.AtomicLong

object SingletonDP {

  //Creational Design Patterns from fundamentals to production-level Scala,
  // using examples from booking systems, payment gateways, and backend services.
  //For every pattern, we'll follow this approach:
  //1. What: What is the pattern?
  //2. Why: What problem does it solve?
  //3. How: How does it work in Scala?
  //4. Why not: When should we avoid it?
  //5. Practice: Implement it ourselves.

  //1. What are Creational Design Patterns?
  // normally we create object like this
  // val payment = new PaymentService()
  //But object creation becomes interesting when:
  //- We need only one instance of an object.
  //- We need to choose an implementation at runtime.
  //- Object construction requires multiple steps.
  //- We need to create families of related objects.
  //- We want to create objects by copying existing ones.

  //Creational DP is all about the objects

  //Singleton - one instance

  //Singleton ensures that only one instance of a class exists within a defined scope and provides a way to access it

  //imagine a backend arch app tracks how many booking have created

  class BookingCounterV1 {
    private var count = 0

    def inc(): Unit = count += 1

    def total: Int = count
  }

  // in this we created two separate instances
  val count1 = new BookingCounterV1()
  val count2 = new BookingCounterV1()

  // But our requirement is to maintain a single implementation count for this app process
  // we need a single shared instance

  //Step 2 - How Scala solves this
  // In java we traditionally implement singleton using a private const and a static instance
  // In Scala we have a built in lang const

  //What happens during execution?
  //When the application first accesses BookingCounter, the JVM initializes the Scala object.
  //Subsequent accesses use the same instance, assuming the same class loader.
  //An important limitation: If your booking application runs on five servers, each server has its own BookingCounter.
  //Singleton doesn't provide a globally shared instance across a distributed system.
  object BookingCounterV2 {
    private var count1 = 0
    private val count = new AtomicLong(0) //We can use AtomicLong:

    def inc() = count.incrementAndGet() //is not atomic. Multiple threads can overwrite each other's updates.

    def total = count
  }

  //now
  //Step 4. Make our example thread-safe
  //Suppose 1,000 users book tickets concurrently.
  //This operation:

  //Step 5. When should we avoid Singleton?
  //Singleton can introduce problems when used incorrectly:
  //- Global mutable state: Any part of the application can change it.
  //- Testing difficulty: State can leak between tests.
  //- Hidden dependencies: Components access the singleton rather than declaring dependencies.
  //- Distributed systems: One instance per JVM isn't one instance across the whole application.

  //For an HTTP client, payment gateway, database connection pool, or other stateful resource,
  // a lifecycle-managed instance passed through constructor injection is often preferable to a global object.
  //However, object remains excellent for stateless utilities, companion objects, constants, and pure functions.


  //Step 6. Singleton vs Companion Object


  class PaymentService(val provider: String) {
    def pay(): Unit =
      println(s"Payment is done via $provider")
  }

  object PaymentService {
    def apply(provider: String): PaymentService = new PaymentService(provider)
  }


  def main(args: Array[String]): Unit = {
    println(count1.inc())
    println(count1.inc())

    println(count2.inc())

    println(count1.total) // 2
    println(count2.total) // 1

    //this is second case
    BookingCounterV2.inc()
    BookingCounterV2.inc()
    println(BookingCounterV2.total) // this will print 2

    //Notice:
    //- We don't use new BookingCounter().
    //- We access methods directly through BookingCounter.
    //- Scala manages the singleton instance.
    //- On the JVM, object initialization is lazy and thread-safe.
    //- The mutable count itself is not thread-safe.

    //The last point is important. Singleton initialization and safe concurrent access to mutable state are two different problems.

    //Campanion Object calling
    val p1 = PaymentService("UPI")
    val p2 = PaymentService("Card")
    println(p1 eq p2)
    //Here:
    //- object PaymentService is a singleton companion object.
    //- PaymentService is a class that can have multiple instances.
    //- Calling the companion's apply() creates new class instances.
    //- Having a companion object doesn't make the associated class
  }
}
