package org.mct.hackernews

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc.{AhcWSClientConfig, StandaloneAhcWSClient}
import play.api.libs.ws.{StandaloneWSClient, WSClientConfig}

trait WSClient {
  def withWSClient[T](block: StandaloneWSClient => T): T = {
    implicit val system: ActorSystem = ActorSystem("WSClient")
    implicit val mat: ActorMaterializer = ActorMaterializer()
    val wsClient = StandaloneAhcWSClient(AhcWSClientConfig(WSClientConfig()))
    try {
      block(wsClient)
    } finally {
      wsClient.close()
      mat.shutdown()
    }
  }
}