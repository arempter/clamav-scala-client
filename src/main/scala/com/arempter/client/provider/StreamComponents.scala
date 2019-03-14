package com.arempter.client.provider

import akka.stream.FlowShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Sink, StreamConverters}
import akka.util.ByteString
import com.arempter.client.config.ClientSettings
import com.arempter.client.data.SocketIO
import com.arempter.client.provider.helpers.ClamAVCommands.scanInsteram
import com.typesafe.config.ConfigFactory


trait StreamComponents extends SocketProvider {

  lazy val clientSettings: ClientSettings = ClientSettings(ConfigFactory.load().getConfig("clamav"))

  def scanFlow(implicit as: SocketIO): Flow[ByteString, String, _] = Flow.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val broadcast = b.add(Broadcast[ByteString](1))
    val responseSource = b.add(StreamConverters.fromInputStream(() => as.in))
    val scanInput = b.add(Flow[ByteString].map(scanInsteram))
    val toStringFlow = b.add(Flow[ByteString].map(_.utf8String))
    val sinkIgnore = b.add(Sink.ignore)

    broadcast ~> scanInput ~> sinkIgnore
                              responseSource ~> toStringFlow

    FlowShape(broadcast.in, toStringFlow.out)
  })

  val resultSink = Sink.head[String]

}
