package DesignPattern.CreattionalDP

import java.time.chrono.ChronoLocalDate

//What is Abstract Factory?
//Definition: Abstract Factory provides an interface for creating families of related or dependent objects without specifying their concrete classes.

//The important word here is families.
//Factory Method generally focuses on creating a product through an overridable method.
//Abstract Factory focuses on creating multiple related products that should work together.

//Real-world example: AWS vs Azure
//Imagine you're building a cloud infrastructure application.
//Your application needs two services:
//1. Storage to upload files.
//2. Messaging to publish events.
//You support two cloud providers:
//- AWS: S3 and SQS
//- Azure: Blob Storage and Service Bus

//simple approach

class Application(provider: String) {
  def uploadFiles(fileName: String): Unit = {
    if (provider == "AWS") {
      println("File uploaded to AWS")
    } else {
      println("File uploaded to Azure")
    }
  }

  def publishEventMessage(): Unit = {
    if (provider == "AWS") {
      println("publish event  to AWS")
    } else {
      println("publish event to Azure")
    }
  }
}

//Problems
//Every time we add a new cloud provider, we modify the application.
//The application knows about concrete provider choices. It also duplicates selection logic across operations.
//Suppose tomorrow we support GCP. We'd have to modify multiple methods.
//Worse, if each product is configured independently, we could accidentally use AWS storage and Azure messaging when our intended policy is to use a single provider.
//Abstract Factory makes that provider-family decision explicit.


//3. How to implement Abstract Factory in Scala

// We'll build the solution step by step.
//Step 1: Create product abstractions
//We need two types of products.

trait CloudStorage {
  def uploadFiles(file: String): Unit
}

trait MessageQueue {
  def publishEvent(msg: String): Unit
}
// These are our abstract product
//Step 2: Create concrete AWS products

class S3Storage extends CloudStorage {

  override def uploadFiles(file: String): Unit = println(s"File uploaded to AWS $file")
}

class SQSQueue extends MessageQueue {

  override def publishEvent(msg: String): Unit = println(s"Event publish to AWS $msg")
}

//Step 3: Create concrete Azure products
class AzureBlobStorage extends CloudStorage {

  override def uploadFiles(file: String): Unit = println(s"File uploaded to Azure $file")
}

class AzureServiceBus extends MessageQueue {

  override def publishEvent(msg: String): Unit = println(s"Event publish to Azure $msg")
}

//Step 4: Create the Abstract Factory
//This is the most important step.

trait CloudFactory {
  def createStorage(): CloudStorage

  def createMessageBus(): MessageQueue
}

//Notice that the factory defines methods for creating both products.
//It doesn't specify how they're created.

//Step 5: Implement Concrete Factories

class AWSFactory extends CloudFactory {

  override def createStorage(): CloudStorage = new S3Storage()

  override def createMessageBus(): MessageQueue = new SQSQueue()
}

class AzureFactory extends CloudFactory {

  override def createStorage(): CloudStorage = new AzureBlobStorage()

  override def createMessageBus(): MessageQueue = new AzureServiceBus()
}

//Now each factory creates its own family.
//AWSFactory creates AWS services.
//AzureFactory creates Azure services.
//Step 6: Create the client application

class CloudApplication(factory: CloudFactory) {
  private val storage = factory.createStorage()
  private val message = factory.createMessageBus()

  def execute():Unit = {
    storage.uploadFiles("Invoice.pdf")
    message.publishEvent("InvoiceUploaded")
  }

  //Look carefully.
  //CloudApplication knows nothing about AWS or Azure.
  //It knows only:
  //- CloudFactory
  //- CloudStorage
  //- MessageQueue
  //That's dependency inversion and family-based object creation working together.
}

object AbstractFactoryDP {

  def main(args: Array[String]): Unit = {
    val awsApp = new CloudApplication(new AWSFactory())
    awsApp.execute()

    val azureApp = new CloudApplication(new AzureFactory())
    azureApp.execute()
  }

}
