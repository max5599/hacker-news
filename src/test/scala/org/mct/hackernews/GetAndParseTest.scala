package org.mct.hackernews

import org.scalatest.FlatSpec
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.concurrent.ExecutionContext.Implicits.global

class GetAndParseTest extends FlatSpec with Test with SimulatedHackerNews with WSClient {

  "GetAndParse" should "get the data from URL and parse them" in {
    val reads = readsThatReturn(JsSuccess("ok"))
    get(Ok(JsString("some json")), reads) shouldBe Right("ok")
  }

  it should "return the json error when parsing fail" in {
    val jsError = JsError("parsing error")
    val reads = readsThatReturn(jsError)
    get(Ok(JsString("some json")), reads) shouldBe Left(ParsingError(jsError))
  }

  it should "return the status error when status is >= 400" in {
    val reads = readsThatReturn(JsSuccess("ok"))
    get(BadRequest, reads) shouldBe Left(HttpStatusError(400))
  }

  private def get[T](result: Result, reads: Reads[String]): Either[Error, String] = withHackerNewsServer(topStoriesResult = result) { url =>
    withWSClient { implicit ws =>
      val getAndParse = new GetAndParse(url + "/v0/topstories", reads)
      getAndParse().value.futureValue
    }
  }

  private def readsThatReturn(jsResult: JsResult[String]): Reads[String] = (_: JsValue) => jsResult
}
