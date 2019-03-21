package com.arempter.client.provider

import java.net.Socket

import com.arempter.client.config.ClientSettings
import com.arempter.client.data.SocketIO
import com.typesafe.config.ConfigFactory

import scala.util.{ Failure, Success, Try }

trait SocketProvider {
  lazy val clientSettings: ClientSettings = ClientSettings(ConfigFactory.load().getConfig("clamav"))

  def getSocketInOut(host: String = clientSettings.clamdHost, port: Int = clientSettings.clamdPort): SocketIO = {
    Try {
      val s = new Socket(host, port)
      s.setSoTimeout(clientSettings.clamdSocketTimeout)
      SocketIO(s.getInputStream, s.getOutputStream)
    } match {
      case Success(r) => r
      case Failure(_) => throw new Exception("Failed to connect to ClamAV")
    }
  }

}
