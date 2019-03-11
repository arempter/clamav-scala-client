package com.arempter.client.provider

import java.io.InputStream
import java.nio.ByteBuffer

import akka.Done
import akka.actor.ActorSystem
import akka.stream.javadsl.RunnableGraph
import akka.stream.scaladsl.{Flow, GraphDSL, Sink, StreamConverters}
import akka.stream.{ActorMaterializer, ClosedShape, Materializer}
import akka.util.ByteString
import com.arempter.client.config.ClientSettings
import com.arempter.client.data.clientImplicits.Command
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

class ClamAVStreamClient(implicit system: ActorSystem) extends BaseProvider {

  implicit lazy val clientSettings: ClientSettings = ClientSettings(ConfigFactory.load().getConfig("clamav"))
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher // separate thread pool

  def scanInputStream(is: InputStream): Future[String] = {

    val as = getSocketInOut(clientSettings.clamdHost, clientSettings.clamdPort)

    def scanHelper(bs: ByteString): Done = {
      as.out.write(Command("zINSTREAM\0").toBytes)
      as.out.flush()
      as.out.write(ByteBuffer.allocate(4).putInt(bs.length).array())
      as.out.write(bs.toArray)
      as.out.write(Array[Byte](0, 0, 0, 0))
      as.out.flush()
      Done
    }

    val resultPrinter = Sink.head[String]

    val scanGraph = RunnableGraph.fromGraph(GraphDSL.create(resultPrinter) { implicit b => resultShape =>
      import GraphDSL.Implicits._

      val isSource = b.add(StreamConverters.fromInputStream(() => is))
      val responseSource = b.add(StreamConverters.fromInputStream(() => as.in))
      val scanFlow = b.add(Flow[ByteString].map(scanHelper))
      val toStringFlow = b.add(Flow[ByteString].map(_.utf8String))
      val sinkIgnore = b.add(Sink.ignore)

      isSource.out ~> scanFlow ~> sinkIgnore
                                responseSource ~> toStringFlow ~> resultShape

      ClosedShape
    })

    scanGraph.run(materializer)
  }

  private def isClean(scanResult: String): Boolean =
    scanResult.contains("OK") || !scanResult.contains("FOUND")

  def scanStream(inputStream: InputStream): Future[String] =
    scanInputStream(inputStream).map { scanResult =>
      println("Response: " + scanResult)
      isClean(scanResult) match {
        case true => "ok"
        case false => "noOK"
      }
    }

}
