package org.mct.hackernews

import org.scalatest.{FeatureSpec, GivenWhenThen}

import scala.concurrent.ExecutionContext.Implicits.global

class TopStoriesFeature extends FeatureSpec with GivenWhenThen with SimulatedHackerNews with Test with WSClient {

  scenario(
    """Using hacker-news api, the top 30 stories should be return with:
      |- the story title
      |- the top 10 commenters of the story and for each commenter also return:
      | - the number of comments the made on the story
      | - The total number of comments they made among all the top 30 stories
    """.stripMargin) {

    Given("Stories, comments and user details")
    val users = (0 to 10).map(i => s"User$i")
    val story1 = storyWithCommentsBy(id = 1,
      11 -> users(0), 10 -> users(1), 9 -> users(2), 8 -> users(3), 7 -> users(4),
      6 -> users(5), 5 -> users(6), 4 -> users(7), 3 -> users(8), 2 -> users(9),
      1 -> users(10)
    )
    val story2 = storyWithCommentsBy(id = 2, 1 -> users(0), 3 -> users(1), 2 -> users(2))
    val otherStories = (3 to 36).map(i => APIStory(id = i, List.empty)).toList

    When("the top stories are retrieved from the API")
    val topStories = withHackerNewsAndStories(story1 :: story2 :: otherStories) { url =>
      withWSClient { implicit ws =>
        val retrieveTopStories = RetrieveTopStories(url)
        retrieveTopStories().value
      }
    }.futureValue

    Then("the result should return the top stories with the title and commenters details")
    val topStory1 = TopStory(story1.title, List(
      TopCommenter(users(0), 11, 12), TopCommenter(users(1), 10, 13), TopCommenter(users(2), 9, 11), TopCommenter(users(3), 8, 8),
      TopCommenter(users(4), 7, 7), TopCommenter(users(5), 6, 6), TopCommenter(users(6), 5, 5), TopCommenter(users(7), 4, 4),
      TopCommenter(users(8), 3, 3), TopCommenter(users(9), 2, 2)
    ))
    val topStory2 = TopStory(story2.title, List(TopCommenter(users(1), 3, 13), TopCommenter(users(2), 2, 11), TopCommenter(users(0), 1, 12)))
    val otherTopStories = otherStories.take(28).map(s => TopStory(s.title, List.empty))

    topStories shouldBe Right(topStory1 :: topStory2 :: otherTopStories)
  }

  private var currentCommentId = 1000

  private def storyWithCommentsBy(id: Int, nbCommentsBy: (Int, String)*) = {
    val comments = nbCommentsBy.flatMap { case (nbComments, user) =>
      (1 to nbComments).map { _ =>
        currentCommentId += 1
        APIComment(currentCommentId, user)
      }
    }.toList
    APIStory(id, comments)
  }
}

