package org.mct.hackernews

import cats.data.EitherT
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.{ExecutionContext, Future}

class GetTopStories(url: String)(implicit ec: ExecutionContext, ws: StandaloneWSClient) extends (() => EitherT[Future, JsError, List[Long]]) {

  private lazy val endpoint = "/v0/topstories"

  override def apply(): EitherT[Future, JsError, List[Long]] = {
    val future = ws.url(url + endpoint).get().map { response =>
      response.body[JsValue].validate[List[Long]] match {
        case JsSuccess(ids, _) => Right(ids)
        case e: JsError => Left(e)
      }
    }
    EitherT(future)
  }
}
