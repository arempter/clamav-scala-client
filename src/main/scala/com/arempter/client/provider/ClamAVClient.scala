package com.arempter.client.provider

import java.io.{ByteArrayInputStream, InputStream}
import java.net.Socket
import java.nio.ByteBuffer

import com.arempter.client.config.ClientSettings
import com.arempter.client.data.SocketIO
import com.arempter.client.data.clientImplicits.{Command, PingCommand}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success, Try}

trait ClamAVClient {
  val clientSettings: ClientSettings
  implicit val executionContext: ExecutionContext

  private val SOCKET_TIMEOUT = clientSettings.clamdSocketTimeout
  val as: SocketIO = getSocketInOut(clientSettings.clamdHost, clientSettings.clamdPort)

  // not used
  def close(): Unit = {
    as.out.write(Command("END\0").toBytes)
    as.out.flush()
    // check if enough
    as.out.close()
    as.in.close()
  }

  def readResponse(inStream: InputStream): String = {
    Source.fromInputStream(inStream).map(_.toString).toList.mkString
  }


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

  def ping(): String = {
    as.out.write(Command(PingCommand.value).toBytes)
    as.out.flush()
    readResponse(as.in)
  }

  def scanChunkStream(chunk: InputStream): Future[String] =
    Future {
      val chunkData = Source.fromInputStream(chunk).map(_.toByte).toArray
      val chunkSize = ByteBuffer.allocate(4).putInt(chunkData.length).array()
      // init
      as.out.write(Command("zINSTREAM\0").toBytes)
      as.out.flush()
      // data
      as.out.write(chunkSize)
      as.out.write(chunkData)
      as.out.write(Array[Byte](0, 0, 0, 0))
      as.out.flush()

      readResponse(as.in)
    }

  def scanStream(chunk: Array[Byte]): Future[String] = {
    scanChunkStream(new ByteArrayInputStream(chunk))
  }
}
