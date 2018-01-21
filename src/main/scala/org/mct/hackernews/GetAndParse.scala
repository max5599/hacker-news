package org.mct.hackernews

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.{StandaloneWSClient, StandaloneWSResponse}

import scala.concurrent.{ExecutionContext, Future}

object GetAndParse {

  def apply[T](url: String)(implicit ec: ExecutionContext, ws: StandaloneWSClient, reads: Reads[T]): EitherT[Future, Error, T] = {
    for {
      response <- getAndValidateStatus(url)
      body <- EitherT.fromEither[Future](parseBody(response))
    } yield body
  }

  private def getAndValidateStatus(url: String)(implicit ec: ExecutionContext, ws: StandaloneWSClient): FutureEither[StandaloneWSResponse] = {
    EitherT(ws.url(url).get().map { response =>
      if (response.status < 400)
        Right(response)
      else
        Left(HttpStatusError(response.status))
    })
  }

  private def parseBody[T](response: StandaloneWSResponse)(implicit reads: Reads[T]): ErrorOr[T] =
    response.body[JsValue].validate match {
      case JsSuccess(data, _) => Right(data)
      case e: JsError => Left(ParsingError(e))
    }
}
