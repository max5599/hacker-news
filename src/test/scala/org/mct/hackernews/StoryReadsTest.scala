package org.mct.hackernews

import org.scalatest.FlatSpec
import play.api.libs.json._

class StoryReadsTest extends FlatSpec with Test {

  "StoryReads" should "convert json to story" in {
    val json = Json.obj(
      "id" -> 1,
      "type" -> "story",
      "kids" -> List(11, 12),
      "title" -> "Story"
    )

    readJson(json) shouldBe JsSuccess(Story(id = 1, "Story", List(11, 12)))
  }

  it should "failed if type is not story" in {
    val json = Json.obj(
      "id" -> 1,
      "type" -> "comment",
      "kids" -> List(11, 12),
      "title" -> "Story"
    )

    readJson(json) shouldBe JsError(JsPath(List(KeyPathNode("type"))), JsonValidationError("error.invalid"))
  }

  it should "default comments to an empty list" in {
    val json = Json.obj(
      "id" -> 1,
      "type" -> "story",
      "title" -> "Story"
    )

    readJson(json).map(_.comments) shouldBe JsSuccess(List.empty)
  }

  private def readJson(json: JsValue): JsResult[Story] = json.validate(ItemReads.storyReads)

}
