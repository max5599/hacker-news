package org.mct.hackernews

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.ahc.{AhcWSClientConfig, StandaloneAhcWSClient}
import play.api.libs.ws.{StandaloneWSClient, WSClientConfig}

import scala.concurrent.{ExecutionContext, Future}

trait WSClient {
  def withWSClient[T](block: StandaloneWSClient => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    implicit val system: ActorSystem = ActorSystem("WSClient")
    implicit val mat: ActorMaterializer = ActorMaterializer()
    val wsClient = StandaloneAhcWSClient(AhcWSClientConfig(WSClientConfig()))
    block(wsClient).andThen { case _ =>
      wsClient.close()
      mat.shutdown()
    }
  }
}