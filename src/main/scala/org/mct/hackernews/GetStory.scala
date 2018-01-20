package org.mct.hackernews

import cats.data.EitherT
import play.api.libs.json.JsError

import scala.concurrent.Future

class GetStory(url: String) extends ((Long) => EitherT[Future, JsError, Story]) {
  override def apply(v1: Long): EitherT[Future, JsError, Story] = ???
}
