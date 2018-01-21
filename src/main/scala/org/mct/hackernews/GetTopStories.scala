package org.mct.hackernews

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSResponse}

import scala.concurrent.{ExecutionContext, Future}

class GetTopStories(baseURL: String)(implicit ec: ExecutionContext, ws: StandaloneWSClient)
  extends (() => EitherT[Future, Error, List[Long]]) {

  private lazy val endpoint = "/v0/topstories"

  override def apply(): EitherT[Future, Error, List[Long]] = {
    for {
      response <- getAndValidateStatus(baseURL + endpoint)
      body <- EitherT.fromEither[Future](parseBody(response))
    } yield body
  }

  private def getAndValidateStatus(url: String): FutureEither[StandaloneWSResponse] = {
    EitherT(ws.url(url).get().map { response =>
      if (response.status < 400)
        Right(response)
      else
        Left(HttpStatusError(response.status))
    })
  }

  private def parseBody(response: StandaloneWSResponse): Either[Error, List[Long]] =
    response.body[JsValue].validate[List[Long]] match {
      case JsSuccess(ids, _) => Right(ids)
      case e: JsError => Left(ParsingError(e))
    }
}
