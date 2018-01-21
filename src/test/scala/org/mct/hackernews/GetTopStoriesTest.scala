package org.mct.hackernews

import org.scalatest.FlatSpec
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.concurrent.ExecutionContext.Implicits.global

class GetTopStoriesTest extends FlatSpec with Test with SimulatedHackerNews with WSClient {

  "GetTopStories" should "get top stories from hacker-news API" in {
    getTopStories(topStoriesResult = Ok(Json.toJson(1 to 30))) shouldBe Right(1 to 30)
  }

  it should "return the json error when parsing fail" in {
    getTopStories(topStoriesResult = Ok(JsString("Oups"))).left.value shouldBe a[ParsingError]
  }

  it should "return the status error when status is >= 400" in {
    getTopStories(topStoriesResult = BadRequest) shouldBe Left(HttpStatusError(400))
  }

  private def getTopStories(topStoriesResult: Result): Either[Error, List[Long]] = withHackerNewsServer(topStoriesResult) { url =>
    withWSClient { implicit ws =>
      val getTopStories = new GetTopStories(url)
      getTopStories().value.futureValue
    }
  }
}
