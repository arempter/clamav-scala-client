package com.arempter.client.provider

import java.io.InputStream

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.javadsl.RunnableGraph
import akka.stream.scaladsl.{Flow, GraphDSL, Sink, Source, StreamConverters}
import akka.util.ByteString
import com.arempter.client.config.ClientSettings
import com.arempter.client.data.SocketIO
import com.arempter.client.provider.helpers.ClamAV._
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

class ClamAVStreamClient(implicit system: ActorSystem) extends BaseProvider {

  implicit lazy val clientSettings: ClientSettings = ClientSettings(ConfigFactory.load().getConfig("clamav"))
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher // separate thread pool

  private def withSocketIO(connection: SocketIO)(f: SocketIO => Future[String]): Future[String] = f(connection)

  private def scanShapeGraph(source: Source[ByteString, _], sink: Sink[String, Future[String]])
                    (implicit as: SocketIO): Graph[ClosedShape, Future[String]] = GraphDSL.create(sink) { implicit b => resultShape =>
    import GraphDSL.Implicits._

    val isSource = b.add(source)
    val responseSource = b.add(StreamConverters.fromInputStream(() => as.in))
    val scanFlow = b.add(Flow[ByteString].map(scanInsteram))
    val toStringFlow = b.add(Flow[ByteString].map(_.utf8String))
    val sinkIgnore = b.add(Sink.ignore)

    isSource.out ~> scanFlow ~> sinkIgnore
                                responseSource ~> toStringFlow ~> resultShape

    ClosedShape
  }

  private val resultPrinter = Sink.head[String]

  def scanInputStream(is: InputStream): Future[String] =
  withSocketIO(getSocketInOut(clientSettings.clamdHost, clientSettings.clamdPort)) { implicit conn =>
    RunnableGraph.fromGraph(scanShapeGraph(StreamConverters.fromInputStream(() => is), resultPrinter)).run(materializer)
  }

  def scanInputStream(is: ByteString): Future[String] =
    withSocketIO(getSocketInOut(clientSettings.clamdHost, clientSettings.clamdPort)) { implicit conn =>
      RunnableGraph.fromGraph(scanShapeGraph(Source.single(is), resultPrinter)).run(materializer)
    }

  private def isClean(scanResult: String): Boolean =
    scanResult.contains("OK") || !scanResult.contains("FOUND")

  private def checkIfClean(scanResult: String): String = isClean(scanResult) match {
    case true => "OK"
    case false => "noOK"
  }

  def scanStream(inputStream: InputStream): Future[String] =
    scanInputStream(inputStream).map(checkIfClean)

  def scanStream(inputStream: ByteString): Future[String] =
    scanInputStream(inputStream).map(checkIfClean)

}
