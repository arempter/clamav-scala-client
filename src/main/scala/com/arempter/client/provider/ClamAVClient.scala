package com.arempter.client.provider

import java.io.InputStream

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Flow, GraphDSL, RunnableGraph, Sink, Source, StreamConverters}
import akka.util.ByteString
import com.arempter.client.data.{ObjectClean, ObjectInfected, ScanResult, SocketIO}
import com.arempter.client.provider.helpers.ClamAVCommands._

import scala.concurrent.Future

class ClamAVClient(implicit system: ActorSystem) extends SocketProvider {

  implicit val materializer: Materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher // separate thread pool

  private def withSocketIO(connection: SocketIO)(f: SocketIO => Future[String]): Future[String] = f(connection)

  private def scanShapeGraph(source: Source[ByteString, _], sink: Sink[String, Future[String]])(implicit as: SocketIO): Graph[ClosedShape, Future[String]] = GraphDSL.create(sink) { implicit b => resultShape =>
    import GraphDSL.Implicits._

    val isSource = b.add(source)
    val responseSource = b.add(StreamConverters.fromInputStream(() => as.in))
    val scanFlow = b.add(Flow[ByteString].map(scanInsteram))
    val toStringFlow = b.add(Flow[ByteString].map(_.utf8String))
    val sinkIgnore = b.add(Sink.ignore)

    isSource ~> scanFlow ~> sinkIgnore
    responseSource ~> toStringFlow ~> resultShape

    ClosedShape
  }

  private val resultSink = Sink.head[String]

  private def scanInputStream(is: InputStream): Future[String] =
    withSocketIO(getSocketInOut(clientSettings.clamdHost, clientSettings.clamdPort)) { implicit conn =>
      RunnableGraph.fromGraph(scanShapeGraph(StreamConverters.fromInputStream(() => is), resultSink)).run()
    }

  private def scanInputStream(is: ByteString): Future[String] =
    withSocketIO(getSocketInOut(clientSettings.clamdHost, clientSettings.clamdPort)) { implicit conn =>
      RunnableGraph.fromGraph(scanShapeGraph(Source.single(is), resultSink)).run()
    }

  private def scanInputStream(is: Source[ByteString, _]): Future[String] =
    withSocketIO(getSocketInOut(clientSettings.clamdHost, clientSettings.clamdPort)) { implicit conn =>
      RunnableGraph.fromGraph(scanShapeGraph(is, resultSink)).run()
    }

  private def isClean(scanResult: String): Boolean =
    scanResult.contains("OK") || !scanResult.contains("FOUND")

  private def checkIfClean(scanResult: String): ScanResult = isClean(scanResult) match {
    case true  => ObjectClean
    case false => ObjectInfected
  }

  def scanStream(input: InputStream): Future[ScanResult] =
    scanInputStream(input).map(checkIfClean)

  def scanStream(input: ByteString): Future[ScanResult] =
    scanInputStream(input).map(checkIfClean)

  def scanStream(input: Source[ByteString, _]): Future[ScanResult] =
    scanInputStream(input).map(checkIfClean)

}

object ClamAVClient {
  implicit val system: ActorSystem = ActorSystem("avScanner")

  def apply(): ClamAVClient = new ClamAVClient
  def apply(implicit system: ActorSystem): ClamAVClient = new ClamAVClient
}
