package org.mct.hackernews

import cats.data.EitherT
import play.api.libs.json.JsError

import scala.concurrent.Future

class GetComment(url: String) extends ((Long) => EitherT[Future, JsError, Comment]) {
  override def apply(v1: Long): EitherT[Future, JsError, Comment] = ???
}
