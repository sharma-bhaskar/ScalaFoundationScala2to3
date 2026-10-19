package DesignPattern.CreattionalDP

import java.awt.TrayIcon.MessageType

//Problem: Notification Delivery System
//Intermediate
//
//You're building a notification service for an e-commerce application.
//The system currently supports Email and SMS. Tomorrow, it may support WhatsApp.
//Implement the classic Factory Method pattern.

//Requirements

//- Create a Notification trait with send(message: String): Unit.

//- Implement EmailNotification and SMSNotification.
//- Create an abstract NotificationProcessor with a factory method named createNotification().
//- The base processor must provide a concrete process(message: String) method that obtains a notification using the factory method and sends it.
//- Create EmailProcessor and SMSProcessor.
//- Extend the system with WhatsAppProcessor without modifying the base processor.

trait NotificationSystem {
  def send(message: String): Unit
}

class EmailNotification extends NotificationSystem {

  override def send(message: String): Unit = println(s"EmailNotification System $message")
}

class SMSNotification extends NotificationSystem {

  override def send(message: String): Unit = println(s"SMSNotification System $message")
}

abstract class NotificationProcess {
  protected def createNotification(): NotificationSystem

  def process(message: String): Unit = {


    val notification = createNotification()
    notification.send(message)
  }
}

class EmailProcessor extends NotificationProcess {

  override protected def createNotification(): NotificationSystem = new EmailNotification()
}

class SMSProcessor extends NotificationProcess {

  override protected def createNotification(): NotificationSystem = new SMSNotification()
}

object Driver {
  def main(args: Array[String]): Unit = {
    val email: NotificationProcess = new EmailProcessor()
    email.process("Hello New email for you")

    val sms: NotificationProcess =
      new SMSProcessor()

    email.process("Booking confirmed")
    sms.process("OTP: 123456")
  }
}

