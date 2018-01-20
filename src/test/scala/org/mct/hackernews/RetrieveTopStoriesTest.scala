package org.mct.hackernews

import cats.implicits._
import org.mct.hackernews.RetrieveTopStories.FutureEither
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext.Implicits.global

class RetrieveTopStoriesTest extends FlatSpec with Test {

  "Retrieving top stories" should "get top stories ids, title and comments of each story, user of each comment " +
    "then return for each story the best commenters with the number of comments and total number of comments" in {
    val user1 = "User1"
    val user2 = "User2"

    val comment1ForStory1 = 101L
    val comment2ForStory1 = 102L
    val comment3ForStory1 = 103L
    val comment1ForStory2 = 201L
    val comments = Map(comment1ForStory1 -> Comment(user1), comment2ForStory1 -> Comment(user2), comment3ForStory1 -> Comment(user1), comment1ForStory2 -> Comment(user1))

    val story1 = Story(1L, "Story1", List(comment1ForStory1, comment2ForStory1, comment3ForStory1))
    val story2 = Story(2L, "Story2", List(comment1ForStory2))
    val stories = List(story1, story2)

    val retrieveTopStories = new RetrieveTopStories(
      getTopStories = () => stories.map(_.id).pure[FutureEither],
      getStory = (id: Long) => stories.find(_.id == id).get.pure[FutureEither],
      getComment = (id: Long) => comments(id).pure[FutureEither]
    )

    retrieveTopStories().value.futureValue shouldBe Right(Seq(
      TopStory(story1.title, List(TopCommenter(user1, 2, 3), TopCommenter(user2, 1, 1))),
      TopStory(story2.title, List(TopCommenter(user1, 1, 3)))
    ))
  }
}
