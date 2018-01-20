package org.mct.hackernews

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class TopStoriesFeature extends FeatureSpec with GivenWhenThen with SimulatedHackerNews with Matchers with ScalaFutures {

  scenario(
    """Using hacker-news api, the top 30 stories should be return with:
      |- the story title
      |- the top 10 commenters of the story and for each commenter also return:
      | - the number of comments the made on the story
      | - The total number of comments they made among all the top 30 stories
    """.stripMargin) {

    Given("Stories, comments and user details")
    val users = (1 to 12).map(i => s"User$i")
    val story1 = storyWithCommentsBy(1,
      5 -> users(1), 4 -> users(2), 3 -> users(3), 3 -> users(4), 3 -> users(5),
      3 -> users(6), 2 -> users(7), 2 -> users(8), 2 -> users(9), 2 -> users(10),
      1 -> users(11)
    )
    val story2 = storyWithCommentsBy(2, 1 -> users(1), 3 -> users(2), 2 -> users(3))
    val story3 = storyWithCommentsBy(3, 3 -> users(1), 2 -> users(6), 4 -> users(7))
    val otherStories = (4 to 36).map(i => APIStory(i, List.empty)).toList

    When("the top stories are retrieved from the API")
    val topStories = withHackerNews(story1 :: story2 :: story3 :: otherStories) { url =>
      val retrieveTopStories = RetrieveTopStories(url)
      retrieveTopStories().value.futureValue
    }

    Then("the result should return the top stories with the title and commenters details")
    val topStory1 = TopStory(story1.title, Seq(
      TopCommenter(users(1), 5, 9), TopCommenter(users(2), 4, 7), TopCommenter(users(3), 3, 5), TopCommenter(users(4), 3, 3),
      TopCommenter(users(5), 3, 3), TopCommenter(users(6), 3, 5), TopCommenter(users(7), 2, 6), TopCommenter(users(8), 2, 2),
      TopCommenter(users(9), 2, 2), TopCommenter(users(10), 2, 2)
    ))
    val topStory2 = TopStory(story2.title, Seq(TopCommenter(users(2), 3, 7), TopCommenter(users(3), 2, 5), TopCommenter(users(1), 1, 9)))
    val topStory3 = TopStory(story2.title, Seq(TopCommenter(users(7), 4, 6), TopCommenter(users(1), 3, 9), TopCommenter(users(6), 2, 5)))
    val otherTopStories = otherStories.take(17).map(s => TopStory(s.title, Seq.empty))

    topStories shouldBe  topStory1 :: topStory2 :: topStory3 :: otherTopStories
  }

  private def storyWithCommentsBy(storyId: Int, nbCommentsBy: (Int, String)*) = {
    val comments = nbCommentsBy.flatMap { case (nbComments, user) => (1 to nbComments).map(i => APIComment(storyId * 1000 + i, user)) }.toList
    APIStory(storyId, comments)
  }
}

