package com.Drone.Producer

import java.awt.Dimension

import org.apache.log4j.Logger

import scala.swing.Dialog.Message
import scala.swing._
import scala.swing.event.ButtonClicked
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

object DroneProducerApp extends SimpleSwingApplication{
  def top = new MainFrame {
    val logger = Logger.getLogger(DroneProducerApp.getClass)
    preferredSize = new Dimension(500, 140)

    title = "Drone producer"

    import DroneProducer._

    private var totalMessagesSent: Int = 0

    def sendMessage: Unit = {
          val drones = generateRandomDroneData(droneIds)
          drones.foreach { d => logger.info(s"Generated drones data:${d}") }

          try {
            for (d <- drones) {


              logger.info(s"Sending message:${d.toJson()}")

              val results = droneProducer.send(d.toJson)
              results match {
                case Success(_) => {
                  totalMessagesSent += 1
                  totalSentMessagesTxt.text = totalMessagesSent.toString
                  logger.info("Message was successfully sent.")
                }
                case Failure(e) => throw e
              }

            }
          } catch {
            case NonFatal(e) => {
              timer.stop()

              logger.error("Failed sending message", e)
              Dialog.showMessage(contents.head, e.getMessage, title = "Error", Message.Error)
            }
          }



    }
    val onTimer = new javax.swing.AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent) = sendMessage
    }
    val rnd = new scala.util.Random

    val timer = new javax.swing.Timer(3000+rnd.nextInt(5000), onTimer)

    val dronesLbl = new Label {
      text = "Drones:"
    }

    val dronesTxt = new TextField {
      text = "3"
    }
    val serverLbl = new Label {
      text = "Server:"
    }
    val serverTxt = new TextField {
      text = "localhost:9092"
    }
    val topicLbl = new Label {
      text = "Topic:"
    }
    val topicTxt = new TextField {
      text = "drone-topic"
    }

    val totalSentMessagesLbl = new Label {
      text = "Total sent:"
    }
    val totalSentMessagesTxt = new TextField {
      text = "0";
      editable = false
    }
    val startBtn = new Button("Start")
    val stopBtn = new Button("Stop")
    listenTo(startBtn, stopBtn)

    reactions += {
      case ButtonClicked(`startBtn`) => startSending
      case ButtonClicked(`stopBtn`) => stopSending
    }

    contents = new BoxPanel(Orientation.Vertical) {
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += dronesLbl
        contents += dronesTxt
        contents += Swing.HStrut(10)
        contents += serverLbl
        contents += serverTxt
        contents += Swing.HStrut(10)
        contents += topicLbl
        contents += topicTxt
      }

      contents += Swing.VStrut(40)
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += startBtn
        contents += Swing.HStrut(10)
        contents += stopBtn
        contents += Swing.HStrut(100)
        contents += totalSentMessagesLbl
        contents += totalSentMessagesTxt
      }

      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
    var droneProducer: DroneProducer = null
    var droneIds: Array[String] = null

    def startSending: Unit = {
      droneProducer = DroneProducer(dronesTxt.text.toInt, serverTxt.text, topicTxt.text)

      if (!droneProducer.isValid) {
        logger.warn(s"Invalid user input:${droneProducer}")
        Dialog.showMessage(contents.head, "Invalid input. Please specify number of drones, brokers and kafka topic.", title = "Invalid input", Message.Error)
        return
      }

      droneIds = (for (i <- 1 to droneProducer.drones) yield generateGUID()).toArray[String]
      droneIds.foreach { id => logger.info(s"drone with id:${id} was generated.") }

      timer.start()
      logger.info("Timer was started.")
    }

    def stopSending: Unit = {
      logger.info("Stopping timer.")

      droneProducer.close
      logger.info(s"Producer:${droneProducer} was closed.")

      if (timer.isRunning) timer.stop()
      logger.info("Timer was stopped.")
    }
  }
}