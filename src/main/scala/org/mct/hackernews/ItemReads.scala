package org.mct.hackernews

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._


object ItemReads {

  implicit val storyReads: Reads[Story] = (
    (JsPath \ "id").read[Long] and
      (JsPath \ "title").read[String] and
      (JsPath \ "kids").readNullable[List[Long]].map(_.getOrElse(List.empty)) and
      (JsPath \ "type").read(verifying[String](_ == "story"))
    ) ((id, title, kids, _) => Story(id, title, kids))
}
