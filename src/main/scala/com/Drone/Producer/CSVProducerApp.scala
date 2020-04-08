package com.Drone.Producer
import java.awt.Dimension
import org.apache.log4j.Logger
import scala.swing.Dialog.Message
import scala.swing._
import scala.swing.event.ButtonClicked
import scala.util.control.NonFatal
import scala.util.{Failure, Success}
import scala.io.Source

object CSVProducerApp extends SimpleSwingApplication{
  def top = new MainFrame {
    val logger = Logger.getLogger(CSVProducerApp.getClass)
    preferredSize = new Dimension(500, 140)

    title = "CSV Ingestion"
    private var totalLinesSent: Int = 0

    def sendMessage: Unit = {
      val bufferedSource = Source.fromFile("prestaCop.csv")
      for (line <- bufferedSource.getLines) {
        logger.info(s"Reading line :${line}")
        try {
            logger.info(s"Sending line:${line}")
            val results = csvProducer.send(line)
            results match {
              case Success(_) => {
                totalLinesSent += 1
                totalLinesSentTxt.text = totalLinesSent.toString
                logger.info("Line was successfully sent.")
              }
              case Failure(e) => throw e
            }

        }
        catch {
          case NonFatal(e) => {
            timer.stop()

            logger.error("Failed sending line", e)
            Dialog.showMessage(contents.head, e.getMessage, title = "Error", Message.Error)
          }
        }
      }
      bufferedSource.close
      timer.stop()

    }
    val onTimer = new javax.swing.AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent) = sendMessage
    }
    val rnd = new scala.util.Random

    val timer = new javax.swing.Timer(100, onTimer)

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
      text = "csv-topic"
    }

    val totalSentMessagesLbl = new Label {
      text = "Total lines sent:"
    }
    val totalLinesSentTxt = new TextField {
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
        contents += totalLinesSentTxt
      }

      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
    var csvProducer: CSVProducer = null

    def startSending: Unit = {
      csvProducer = CSVProducer(serverTxt.text, topicTxt.text)
      timer.start()
      logger.info("Timer was started.")
    }

    def stopSending: Unit = {
      logger.info("Stopping timer.")

      csvProducer.close
      logger.info(s"Producer:${csvProducer} was closed.")

      if (timer.isRunning) timer.stop()
      logger.info("Timer was stopped.")
    }
  }
}