package com.arempter.client.config

import com.typesafe.config.Config


class ClientSettings(config: Config) {

  val clamdSocketTimeout = config.getInt("timeout")
  val clamdHost = config.getString("host")
  val clamdPort = config.getInt("port")
}

object ClientSettings {
  def apply(config: Config): ClientSettings = new ClientSettings(config)
}
