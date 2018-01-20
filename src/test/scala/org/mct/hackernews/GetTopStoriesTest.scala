package org.mct.hackernews

import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext.Implicits.global

class GetTopStoriesTest extends FlatSpec with Test with SimulatedHackerNews with WSClient {

  "GetTopStories" should "get top stories from hacker-news API" in {
    val topStories = (1 to 30).map(APIStory(_, List.empty))
    val topStoriesId = withHackerNews(topStories) { url =>
      withWSClient { implicit ws =>
        val getTopStories = new GetTopStories(url)
        getTopStories().value.futureValue.right.value
      }
    }
    topStoriesId shouldBe (1 to 30)
  }
}
