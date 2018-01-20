package org.mct.hackernews

import cats.data.EitherT
import play.api.libs.json.JsError

import scala.concurrent.Future

class GetTopStories(url: String) extends (() => EitherT[Future, JsError, List[Long]]) {
  override def apply(): EitherT[Future, JsError, List[Long]] = ???
}
