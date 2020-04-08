package com.Drone.Producer

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}

import scala.util.{Failure, Success, Try}

case class CSVProducer(brokers : String, topic: String){
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

}


