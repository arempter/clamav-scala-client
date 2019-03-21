package com.arempter.client.provider.helpers

import java.nio.ByteBuffer

import akka.Done
import akka.util.ByteString
import com.arempter.client.data.SocketIO
import com.arempter.client.data.clientImplicits.Command

object ClamAVCommands {

  def scanInsteram(bs: ByteString)(implicit as: SocketIO): Done = {
    as.out.write(Command("zINSTREAM\u0000").toBytes)
    as.out.flush()
    as.out.write(ByteBuffer.allocate(4).putInt(bs.length).array())
    as.out.write(bs.toArray)
    as.out.write(Array[Byte](0, 0, 0, 0))
    as.out.flush()
    Done
  }

}
