package com.arempter.client.data

import java.nio.charset.StandardCharsets

object clientImplicits {

  sealed trait CommandString {
    val value: String
  }

  case object PingCommand extends CommandString {
    val value = "zPING\u0000" // string instead of value
  }

  case class Command(value: String)

  sealed trait CommandConverter[T] {
    def convert(cmd: T): Array[Byte]
  }

  implicit object byteCommand extends CommandConverter[Command] {
    def convert(cmd: Command): Array[Byte] = cmd.value.getBytes(StandardCharsets.UTF_8)
  }

  implicit class ByteConverters[T](cmd: T) {
    def toBytes(implicit converter: CommandConverter[T]): Array[Byte] = converter.convert(cmd)
  }

}

