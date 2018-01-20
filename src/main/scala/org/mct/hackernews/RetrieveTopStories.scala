package org.mct.hackernews

import cats.data.EitherT
import play.api.libs.json.JsError

import scala.concurrent.Future

class RetrieveTopStories(
                          getTopStories: () => EitherT[Future, JsError, Long],
                          getStory: (Long) => EitherT[Future, JsError, Story],
                          getComment: (Long) => EitherT[Future, JsError, Comment]
                        ) {
  def apply(): EitherT[Future, JsError, TopStory] = ???
}

object RetrieveTopStories {
  def apply(url: String): RetrieveTopStories = {
    val getTopStories = new GetTopStories(url)
    val getStory = new GetStory(url)
    val getComment = new GetComment(url)
    new RetrieveTopStories(getTopStories, getStory, getComment)
  }
}

case class TopStory(title: String, topCommenters: Seq[TopCommenter])

case class TopCommenter(username: String, storyComments: Int, totalcomments: Int)