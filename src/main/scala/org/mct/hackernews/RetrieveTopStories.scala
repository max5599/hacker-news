package org.mct.hackernews

import cats.implicits._
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.ExecutionContext

class RetrieveTopStories(
                          getTopStories: () => FutureEither[List[Long]],
                          getStory: (Long) => FutureEither[Story],
                          getComment: (Long) => FutureEither[Comment]
                        )(implicit ec: ExecutionContext) {

  def apply(): FutureEither[List[TopStory]] = {
    val storiesAndComments = for {
      storiesId <- getFirst30TopStories()
      stories <- getStories(storiesId)
      storyAndComments <- getStoryAndComments(stories)
    } yield storyAndComments
    storiesAndComments.map(aggregate)
  }

  private def getFirst30TopStories(): FutureEither[List[Long]] = getTopStories().map(_.take(30))

  private def getStories(ids: List[Long]): FutureEither[List[Story]] = ids.map(getStory).sequence

  private def getStoryAndComments(stories: List[Story]): FutureEither[List[(Story, List[Comment])]] =
    stories.map(s => getComments(s.comments).map(c => s -> c)).sequence

  private def getComments(ids: List[Long]): FutureEither[List[Comment]] = ids.map(getComment).sequence

  private def aggregate(storiesAndComments: List[(Story, List[Comment])]): List[TopStory] = {
    val totalCommentsByUser: Map[String, Int] = storiesAndComments.flatMap(_._2.toSeq).map(_.by).groupBy(identity).mapValues(_.size)
    storiesAndComments.map { case (story, comments) =>
      val topCommenters = comments.groupBy(_.by).mapValues(_.size)
        .map { case (username, nb) =>
          TopCommenter(username, nb, totalCommentsByUser(username))
        }.toList.sortBy(-_.storyComments).take(10)
      TopStory(story.title, topCommenters)
    }
  }
}

object RetrieveTopStories {
  def apply(url: String)(implicit ec: ExecutionContext, ws: StandaloneWSClient): RetrieveTopStories = {
    import play.api.libs.json.Reads._
    import ItemReads._

    val getTopStories = new GetAndParse[List[Long]]
    val getStory = new GetAndParse[Story]
    val getComment = new GetAndParse[Comment]
    new RetrieveTopStories(
      () => getTopStories(url + "/v0/topstories"),
      id => getStory(url + "/v0/item/" + id),
      id => getComment(url + "/v0/item/" + id)
    )
  }
}