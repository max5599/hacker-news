package org.mct.hackernews

import cats.data.EitherT
import play.api.libs.json.JsError

import scala.concurrent.Future

class GetComment(url: String) extends ((Long) => FutureEither[Comment]) {
  override def apply(v1: Long): FutureEither[Comment] = ???
}
