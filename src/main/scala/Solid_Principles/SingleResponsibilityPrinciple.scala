package Solid_Principles

object SingleResponsibilityPrinciple {

  //Definition: A module should have one reason to change.

  // Imagine - we are building Booking Service

  //Wrong approach

  //What's wrong?
  //The class has four separate reasons to change.
  //If we change the database from MySQL to PostgreSQL, this class may change.
  // If our email provider changes, this class may change. If the invoice format changes, this class may change.
  //That's too much responsibility in one place.

  class BookService {
    def bookTicket(seatId: String, email: String): Unit = {
      // 1. Validate seat availability

      // 2. Save booking to database

      // 3. Send confirmation email

      // 4. Generate invoice PDF
    }
  }

  //Correct Approach:  Separate responsibilities
  case class Booking(seatId: String, email: String)

  trait BookingRepo {
    def save(booking: Booking): Unit
  }

  trait BookingNotifier {
    def sendConfirmation(booking: Booking): Unit
  }

  class BookingService(repo: BookingRepo, notifier: BookingNotifier) {
    def bookTicket(booking: Booking): Unit = {
      repo.save(booking)
      notifier.sendConfirmation(booking)
    }
  }
  //Now each component has a clear responsibility.
  //Notice something important: BookingService still coordinates multiple actions.
  //That does not violate SRP. Its single responsibility is orchestrating the booking use case.


  def main(args: Array[String]): Unit = {

  }
}
