package org.mct.hackernews

import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.JsError

import scala.concurrent.{ExecutionContext, Future}

class RetrieveTopStories(
                          getTopStories: () => EitherT[Future, JsError, List[Long]],
                          getStory: (Long) => EitherT[Future, JsError, Story],
                          getComment: (Long) => EitherT[Future, JsError, Comment]
                        )(implicit ec: ExecutionContext) {
  def apply(): EitherT[Future, JsError, Seq[TopStory]] = {
    val storiesAndComments: EitherT[Future, JsError, Seq[(Story, Seq[Comment])]] = for {
      storiesId <- getTopStories()
      stories <- getStories(storiesId)
      storyAndComments <- getStoryAndComments(stories)
    } yield storyAndComments

    storiesAndComments.map { sAndC =>
      val sAndU = sAndC.map { case (story, comments) =>
        story -> comments.groupBy(_.by).mapValues(_.size)
      }
      val total: Map[String, Int] = sAndU.flatMap(_._2.toSeq).groupBy(_._1).mapValues(_.map(_._2).sum)
      sAndU.map { case (story, users) =>
        TopStory(story.title, users.map { case (username, nb) => TopCommenter(username, nb, total(username)) }.toList.sortBy(-_.storyComments))
      }
    }
  }

  private def getStories(ids: List[Long]): EitherT[Future, JsError, List[Story]] = {
    ids.map(getStory).sequence
  }

  def sequence[T](list: List[EitherT[Future, JsError, T]]): EitherT[Future, JsError, List[T]] = list match {
    case Nil => EitherT[Future, JsError, List[T]](Future.successful(Right(List.empty)))
    case h :: t => h flatMap (hh => sequence(t) map (hh :: _))
  }

  private def getStoryAndComments(stories: List[Story]): EitherT[Future, JsError, List[(Story, List[Comment])]] = {
    sequence(stories.map(s => getComments(s.comments).map(c => s -> c)))
  }

  private def getComments(ids: List[Long]): EitherT[Future, JsError, List[Comment]] = {
    ids.map(getComment).sequence
  }

  /* private def test(): Unit = {
     val list: List[EitherT[Future, String, Int]] = List(1.pure[], 2.pure)
     val eitherOfList: EitherT[Future, String, List[Int]] = list.sequence
   }*/
}

object RetrieveTopStories {
  def apply(url: String)(implicit ec: ExecutionContext): RetrieveTopStories = {
    val getTopStories = new GetTopStories(url)
    val getStory = new GetStory(url)
    val getComment = new GetComment(url)
    new RetrieveTopStories(getTopStories, getStory, getComment)(ec)
  }
}

case class TopStory(title: String, topCommenters: List[TopCommenter])

case class TopCommenter(username: String, storyComments: Int, totalcomments: Int)