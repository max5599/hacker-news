package org.mct.hackernews

import scala.concurrent.Future

object RetrieveTopStories {
  def apply(url: String): Future[Seq[TopStory]] = ???
}

case class TopStory(title: String, topCommenters: Seq[TopCommenter])

case class TopCommenter(username: String, storyComments: Int, totalcomments: Int)