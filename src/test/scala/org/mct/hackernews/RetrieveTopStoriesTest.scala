package org.mct.hackernews

import cats.data.EitherT
import org.scalatest.FlatSpec
import play.api.libs.json.JsError

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RetrieveTopStoriesTest extends FlatSpec with Test {

  "Retrieving top stories" should "get top stories ids, title and comments of each story, user of each comment " +
    "then return for each story the best commenters with the number of comments and total number of comments" in {
    val getTopStories = () => EitherT[Future, JsError, List[Long]](Future.successful(Right(List(1, 2))))
    val stories = Map(1L -> Story(1L, "Story1", List(101, 102, 103)), 2L -> Story(2L, "Story2", List(201)))
    val getStory = (id: Long) => EitherT[Future, JsError, Story](Future.successful(Right(stories(id))))
    val comments = Map(101L -> Comment("User1"), 102L -> Comment("User2"), 103L -> Comment("User1"), 201 -> Comment("User1"))
    val getComment = (id: Long) => EitherT[Future, JsError, Comment](Future.successful(Right(comments(id))))

    val retrieveTopStories = new RetrieveTopStories(getTopStories, getStory, getComment)

    retrieveTopStories().value.futureValue shouldBe Right(Seq(
      TopStory("Story1", List(TopCommenter("User1", 2, 3), TopCommenter("User2", 1, 1))),
      TopStory("Story2", List(TopCommenter("User1", 1, 3)))
    ))
  }
}
