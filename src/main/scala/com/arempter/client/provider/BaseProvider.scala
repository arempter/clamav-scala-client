package com.arempter.client.provider

import java.io.{ByteArrayInputStream, InputStream}
import java.net.Socket

import com.arempter.client.config.ClientSettings
import com.arempter.client.data.SocketIO

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait BaseProvider {
  val clientSettings: ClientSettings

  private val SOCKET_TIMEOUT = clientSettings.clamdSocketTimeout

  def getSocketInOut(host: String = "localhost", port: Int = 3310): SocketIO = {
    Try {
      val s = new Socket(host, port)
      s.setSoTimeout(SOCKET_TIMEOUT)
      SocketIO(s.getInputStream, s.getOutputStream)
    } match {
      case Success(r) => r
      case Failure(_) => throw new Exception("Failed to connect to ClamAV")
    }
  }

  def scanInputStream(is: InputStream): Future[String]

  def scanStream(chunk: Array[Byte]): Future[String] =
    scanInputStream(new ByteArrayInputStream(chunk))

}
