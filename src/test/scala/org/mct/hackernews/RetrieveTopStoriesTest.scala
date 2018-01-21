package org.mct.hackernews

import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global

class RetrieveTopStoriesTest extends UnitTest {

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

    val topStories = retrieveTopStories(
      getTopStories = () => stories.map(_.id),
      getStory = (id: Long) => stories.find(_.id == id).get,
      getComment = (id: Long) => comments(id)
    )

    topStories shouldBe Seq(
      TopStory(story1.title, List(TopCommenter(user1, 2, 3), TopCommenter(user2, 1, 1))),
      TopStory(story2.title, List(TopCommenter(user1, 1, 3)))
    )
  }

  it should "only retrieve the first 30 top stories" in {
    val topStories = retrieveTopStories(
      getTopStories = () => (1L to 35L).toList,
      getStory = (id: Long) => Story(id, s"Story$id", List.empty),
      getComment = (_: Long) => Comment("someone")
    )

    topStories.map(_.title) shouldBe (1L to 30L).map(i => s"Story$i")
  }

  it should "only retrieve the first 10 top commenters" in {
    val topStories = retrieveTopStories(
      getTopStories = () => List(1L),
      getStory = (_: Long) => Story(1L, "Story", (1L to 25L).toList),
      getComment = (id: Long) => if (id <= 20) Comment(s"User${(id % 10) + 1}") else Comment(s"User$id")
    )

    topStories.flatMap(_.topCommenters).map(_.username) should contain only ((1L to 10L).map(i => s"User$i"):_*)
  }

  private def retrieveTopStories(
                                  getTopStories: () => List[Long],
                                  getStory: (Long) => Story,
                                  getComment: (Long) => Comment
                                ) = {
    val f = new RetrieveTopStories(
      getTopStories = () => getTopStories().pure[FutureEither],
      getStory = (id: Long) => getStory(id).pure[FutureEither],
      getComment = (id: Long) => getComment(id).pure[FutureEither]
    )
    f().value.futureValue.right.value
  }
}
