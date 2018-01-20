package org.mct.hackernews

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.JsError

import scala.concurrent.{ExecutionContext, Future}
import RetrieveTopStories.FutureEither

class RetrieveTopStories(
                          getTopStories: () => FutureEither[List[Long]],
                          getStory: (Long) => FutureEither[Story],
                          getComment: (Long) => FutureEither[Comment]
                        )(implicit ec: ExecutionContext) {

  def apply(): FutureEither[List[TopStory]] = {
    val storiesAndComments = for {
      storiesId <- getTopStories()
      stories <- getStories(storiesId)
      storyAndComments <- getStoryAndComments(stories)
    } yield storyAndComments
    storiesAndComments.map(aggregate)
  }

  private def getStories(ids: List[Long]): FutureEither[List[Story]] = ids.map(getStory).sequence

  private def getStoryAndComments(stories: List[Story]): FutureEither[List[(Story, List[Comment])]] =
    stories.map(s => getComments(s.comments).map(c => s -> c)).sequence

  private def getComments(ids: List[Long]): FutureEither[List[Comment]] = ids.map(getComment).sequence

  private def aggregate(storiesAndComments: List[(Story, List[Comment])]): List[TopStory] = {
    val totalCommentsByUser: Map[String, Int] = storiesAndComments.flatMap(_._2.toSeq).map(_.by).groupBy(identity).mapValues(_.size)
    storiesAndComments.map { case (story, comments) =>
      val topCommenters = comments.groupBy(_.by).mapValues(_.size).map { case (username, nb) => TopCommenter(username, nb, totalCommentsByUser(username)) }.toList.sortBy(-_.storyComments)
      TopStory(story.title, topCommenters)
    }
  }
}

object RetrieveTopStories {
  def apply(url: String)(implicit ec: ExecutionContext): RetrieveTopStories = {
    val getTopStories = new GetTopStories(url)
    val getStory = new GetStory(url)
    val getComment = new GetComment(url)
    new RetrieveTopStories(getTopStories, getStory, getComment)(ec)
  }

  type FutureEither[A] = EitherT[Future, JsError, A]
}

case class TopStory(title: String, topCommenters: List[TopCommenter])

case class TopCommenter(username: String, storyComments: Int, totalcomments: Int)