package org.mct.hackernews

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSResponse}

import scala.concurrent.{ExecutionContext, Future}

class GetAndParse[T](implicit ec: ExecutionContext, ws: StandaloneWSClient, reads: Reads[T]) extends (String => FutureEither[T]) {

  override def apply(url: String): EitherT[Future, Error, T] = {
    for {
      response <- getAndValidateStatus(url)
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

  private def parseBody(response: StandaloneWSResponse): ErrorOr[T] =
    response.body[JsValue].validate match {
      case JsSuccess(ids, _) => Right(ids)
      case e: JsError => Left(ParsingError(e))
    }
}
