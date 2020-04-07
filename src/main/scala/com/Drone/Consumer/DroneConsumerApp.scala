package com.Drone.Consumer

import scala.swing._
import scala.swing.event.ButtonClicked
import scala.swing.SimpleSwingApplication
import java.awt.Dimension
import java.io.FileWriter
import scala.swing.Dialog.Message
import org.apache.log4j.Logger

import scala.swing.event.Key.Location
import scala.util.control.NonFatal
import scala.util.parsing.json.JSON
import scala.util.{Failure, Success}

object DroneConsumerApp extends SimpleSwingApplication{
  def top = new MainFrame {
    val logger = Logger.getLogger(DroneConsumerApp.getClass)
    preferredSize = new Dimension(550, 140)

    title = "Alert Drones"

    private var totalReceivedMessages: Int = 0
    private var typeAlert: Int = 0


    def receiveMessages: Unit = {

      try {
        val messages = droneConsumer.receive

        messages match {
          case Success(seq: Seq[String]) => {
            for (m <- seq) {
              logger.info(s"Received message:\n${m}")
              val result = JSON.parseFull(m)
              result match {
                // Matches if jsonStr is valid JSON and represents a Map of Strings to Any
                case Some(map: Map[String,Any]) =>
                  if(map("typeAlert")==0){
                    val fw = new FileWriter("drone_data.json", true)
                    try {
                      fw.write(m)
                    }
                    finally fw.close()
                  }
                  else {
                    Dialog.showMessage(contents.head," Alert Code : "+map("typeAlert")+"\nLocalisation : ("+map("longitude")+", "+map("latitude")+")",title = "New Alert")
                    val fw = new FileWriter("drone_alert.json", true)
                    try {
                      fw.write(m)
                    }
                    finally fw.close()
                  }
                case None => println("Parsing failed")
                case other => println("Unknown data structure: " + other)
              }
              totalReceivedMessages += 1
              totalReceivedMessagesTxt.text = totalReceivedMessages.toString


            }
          }
          case Failure(e) => throw e
        }
      } catch {
        case NonFatal(e) => {
          timer.stop()

          logger.error("Failed receiving messages", e)
          Dialog.showMessage(contents.head, e.getMessage, title = "Error", Message.Error)
        }
      }
    }

    val onTimer = new javax.swing.AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent) = receiveMessages
    }
    val timer = new javax.swing.Timer(2000, onTimer)

    val pollTimeoutLbl = new Label {
      text = "Polling timeout(ms):"
    }
    val pollTimeoutTxt = new TextField {
      text = "500"
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

    val totalReceivedMessagesLbl = new Label {
      text = "Total received:"
    }
    val totalReceivedMessagesTxt = new TextField {
      text = "0";
      editable = false
    }

    val typeAlertLbl = new Label {
      text = "Total received:"
    }
    val typeAlertTxt = new TextField {
      text = "0";
      editable = false
    }
    val startBtn = new Button("Start")
    val stopBtn = new Button("Stop")
    listenTo(startBtn, stopBtn)

    reactions += {
      case ButtonClicked(`startBtn`) => startReceiving
      case ButtonClicked(`stopBtn`) => stopReceiving
    }

    contents = new BoxPanel(Orientation.Vertical) {
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += pollTimeoutLbl
        contents += pollTimeoutTxt
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
        contents += totalReceivedMessagesLbl
        contents += totalReceivedMessagesTxt
      }

      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
    var droneConsumer: DroneConsumer = null

    def startReceiving: Unit = {
      droneConsumer = DroneConsumer(serverTxt.text, pollTimeoutTxt.text.toInt, topicTxt.text)

      if (!droneConsumer.isValid) {
        logger.warn(s"Invalid user input:${droneConsumer}")
        Dialog.showMessage(contents.head, "Invalid input. Please specify poll timeout, brokers and kafka topic.", title = "Invalid input", Message.Error)
        return
      }

      timer.start()
      logger.info("Timer was started.")
    }

    def stopReceiving: Unit = {
      logger.info("Stopping timer.")

      droneConsumer.close
      logger.info(s"Consumer:${droneConsumer} has been closed.")

      if (timer.isRunning) timer.stop()
      logger.info("Timer has been stopped.")
    }
  }
}
