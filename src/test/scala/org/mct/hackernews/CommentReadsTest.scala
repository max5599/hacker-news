package org.mct.hackernews

import play.api.libs.json._

class CommentReadsTest extends UnitTest {

  "CommentReads" should "convert json to comment" in {
    val json = Json.obj(
      "by" -> "max",
      "type" -> "comment"
    )

    readJson(json) shouldBe JsSuccess(Comment("max"))
  }

  it should "failed if type is not comment" in {
    val json = Json.obj(
      "by" -> "max",
      "type" -> "story"
    )

    readJson(json) shouldBe JsError(JsPath(List(KeyPathNode("type"))), JsonValidationError("error.invalid"))
  }

  private def readJson(json: JsValue): JsResult[Comment] = json.validate(ItemReads.commentReads)

}
