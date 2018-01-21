package org.mct.hackernews

import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.concurrent.ExecutionContext.Implicits.global

class GetAndParseTest extends UnitTest with SimulatedHackerNews with WSClient {

  "GetAndParse" should "get the data from URL and parse them" in {
    implicit val reads: Reads[String] = readsThatReturn(JsSuccess("ok"))
    getAndParse(Ok(JsString("some json"))) shouldBe Right("ok")
  }

  it should "return the json error when parsing fail" in {
    val jsError = JsError("parsing error")
    implicit val reads: Reads[String] = readsThatReturn(jsError)
    getAndParse(Ok(JsString("some json"))) shouldBe Left(ParsingError(jsError))
  }

  it should "return the status error when status is >= 400" in {
    implicit val reads: Reads[String] = readsThatReturn(JsSuccess("ok"))
    getAndParse(BadRequest) shouldBe Left(HttpStatusError(400))
  }

  private def getAndParse(result: Result)(implicit reads: Reads[String]): Either[Error, String] =
    withHackerNewsServer(topStoriesResult = result) { url =>
      withWSClient { implicit ws =>
        GetAndParse(url + "/v0/topstories.json").value
      }
    }.futureValue

  private def readsThatReturn(jsResult: JsResult[String]): Reads[String] = (_: JsValue) => jsResult
}
