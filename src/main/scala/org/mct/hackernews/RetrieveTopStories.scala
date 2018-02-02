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
      storiesId <- first30TopStories()
      storyAndComments <- storiesAndCommentsFromIds(storiesId)
    } yield storyAndComments
    storiesAndComments.map(aggregate)
  }

  private def first30TopStories(): FutureEither[List[Long]] = getTopStories().map(_.take(30))

  private def storiesAndCommentsFromIds(ids: List[Long]): FutureEither[List[(Story, List[Comment])]] =
    ids.map { storyId =>
      for {
        story <- getStory(storyId)
        comments <- getComments(story.comments)
      } yield story -> comments
    }.sequence[FutureEither, (Story, List[Comment])]

  private def getComments(ids: List[Long]): FutureEither[List[Comment]] = ids.map(getComment).sequence[FutureEither, Comment]

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

    new RetrieveTopStories(
      () => GetAndParse[List[Long]](url + "/v0/topstories.json"),
      id => GetAndParse[Story](url + "/v0/item/" + id + ".json"),
      id => GetAndParse[Comment](url + "/v0/item/" + id + ".json")
    )
  }
}