package org.mct

import cats.data.EitherT
import play.api.libs.json.JsError

import scala.concurrent.Future

package object hackernews {

  type FutureEither[A] = EitherT[Future, JsError, A]

  case class Comment(by: String)

  case class Story(id: Long, title: String, comments: List[Long])

  case class TopStory(title: String, topCommenters: List[TopCommenter])

  case class TopCommenter(username: String, storyComments: Int, totalcomments: Int)

}
