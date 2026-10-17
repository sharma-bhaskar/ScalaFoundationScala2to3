package Solid_Principles

object InterfaceSegregationPrinciple {

  //Definition: A client should not be forced to depend on methods it does not use.

  //Wrong approach: Fat interface

  trait BookingOperation {
    def reserveSeat(seatId: String): Unit

    def cancelBooking(bookId: String): Unit

    def exportSalesReport(): String
  }

  //Imagine a component responsible only for seat reservations.
  //It shouldn't need to depend on cancellation and reporting operations.


  //Correct approach: Small interfaces

  trait SeatReservation {
    def reserveSeat(seatId: String): Unit
  }

  trait BookingCancellation {
    def cancelBooking(bookingId: String): Unit
  }

  trait SalesReporting {
    def exportSalesReport(): String
  }

  class ReservationController(reservation: SeatReservation) {
    def reserve(seatId: String): Unit = reservation.reserveSeat(seatId)
  }

  //Now ReservationController depends only on the behavior it needs.
  //What if one implementation supports all three?
  //Scala permits composing them:

  class BookingManager
    extends SeatReservation
      with BookingCancellation
      with SalesReporting {

    override def reserveSeat(seatId: String): Unit = println("reserveSeat")

    override def cancelBooking(bookingId: String): Unit = println("cancelBooking")

    override def exportSalesReport(): String = "exportSales"
  }

  def main(args: Array[String]): Unit = {

  }
}
