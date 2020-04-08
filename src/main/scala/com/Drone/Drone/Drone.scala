package com.Drone.Drone

case class DroneDevice(droneId: String, temperature: Int, latitude: Double, longitude: Double, alertType: Int, time: Long){

  def canEqual(a: Any) = a.isInstanceOf[DroneDevice]

  override def equals(that: Any): Boolean =
    that match {
      case that: DroneDevice => that.canEqual(this) &&
        this.droneId == that.droneId &&
        this.temperature == that.temperature &&
        //alertype==0 means its classic drone data without alert
        this.alertType==that.alertType &&
        this.latitude == that.latitude&&
        this.longitude==that.longitude
      case _ => false
    }

  def toJson() : String = {
    s"""{"deviceId": "${droneId}","temperature": ${temperature},"latitude": ${latitude},"longitude": ${longitude},"typeAlert": ${alertType},"time": ${time}}""".stripMargin
  }
}

object DroneDevice {
  def parse(json: String) : Option[DroneDevice] = {
    import org.json4s._
    implicit val formats = DefaultFormats
    val data = (org.json4s.jackson.JsonMethods.parse(json) \ "data").toOption

    data match {
      case Some(_) => Some(data.get.extract[DroneDevice])
      case _ => None
    }
  }
}