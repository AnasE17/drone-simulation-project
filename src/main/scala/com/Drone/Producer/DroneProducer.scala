package com.Drone.Producer

import java.util.Properties

import com.Drone.Drone.DroneDevice
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.log4j.Logger

import scala.util.{Failure, Success, Try}
import scala.math.BigInt

case class DroneProducer(drones: Int = 3, brokers : String, topic: String){
//  require(devices > 0 && brokers.nonEmpty && topic.nonEmpty)

  private final val serializer = "org.apache.kafka.common.serialization.StringSerializer"
  private lazy val currentKafkaProducer: Try[KafkaProducer[String, String]] = Try(new KafkaProducer[String, String](configuration))
  private def configuration: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("key.serializer", serializer)
    props.put("value.serializer", serializer)
    props
  }

  def send(msg: String): Try[java.util.concurrent.Future[RecordMetadata]] = {

    currentKafkaProducer match {
      case Success(p) => {

            val data = new ProducerRecord[String, String](topic, msg)
        Success(p.send(data))
      }
      case Failure(e) => Failure(e)
    }

  }

  def close:Unit = {
    currentKafkaProducer match {
      case Success(p) => {
        p.close()
      }
      case Failure(e) => throw e
    }
  }

  def isValid : Boolean = drones > 0 && brokers.nonEmpty && topic.nonEmpty
}

object DroneProducer {
  final val minTemp = -10
  final val maxTemp = 50
  final val minAlert = 1
  final val maxAlert = 10
  private var id: Long = 0
  private var isAlert: Int = 0
  private val rnd = new scala.util.Random
  val logger = Logger.getLogger(DroneProducer.getClass)
  //function to generate random data
  def generateRandomDroneData(deviceIds: Array[String]): Array[DroneDevice] = {

        val devices = new Array[DroneDevice](deviceIds.length)

        for (i <- 0 to devices.length - 1) {
          devices(i) = DroneDevice(deviceIds(i), generateTemperature, rnd.nextDouble(),rnd.nextDouble(),generateAlert,System.currentTimeMillis)
        }
        devices
      }

//random temmp
  def generateTemperature: Int = {
    val rnd = new scala.util.Random
    minTemp + rnd.nextInt((maxTemp - minTemp) + 1)
  }

  //we generate a random number between 1 and 10 if its equal to 5 we genrate a random alert
  def generateAlert: Int = {
    val rnd = new scala.util.Random
    var normal=0
    if (minAlert + rnd.nextInt((maxAlert - minAlert) + 1)==5){
      normal=minAlert + rnd.nextInt((maxAlert - minAlert) + 1)
      normal
    }
    else{
      normal
    }
  }
  import java.util.UUID
  //function to create an id
  def generateGUID(): String = {
    val uuid = new UUID(rnd.nextLong(), rnd.nextLong())
    uuid.toString
  }
}
