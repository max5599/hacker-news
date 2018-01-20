package org.mct.hackernews

import akka.actor.ActorSystem
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.routing.{Router, SimpleRouterImpl}
import play.api.{BuiltInComponents, NoHttpFiltersComponents}
import play.core.server.{AkkaHttpServerComponents, ServerConfig}
import play.api.mvc.Results._
import play.api.routing.sird._

import scala.concurrent.Future

trait SimulatedHackerNews {

  def withHackerNews[T](stories: Seq[APIStory])(block: String => T): T = {
    val items: Map[Long, JsValue] = itemsFrom(stories)

    val hackerNewsServer = createServer {
      action => {
        case GET(p"/v0/topstories") => action(Ok(Json.toJson(stories.map(_.id))))
        case GET(p"/v0/item/${long(id)}") => action(Ok(items(id)))
      }
    }.server
    try {
      block(s"http://localhost:${hackerNewsServer.httpPort.get}")
    } finally {
      hackerNewsServer.stop()
    }
  }

  private def createServer(serverRoutes: ActionBuilder[Request, AnyContent] => PartialFunction[RequestHeader, Handler]) =
    new AkkaHttpServerComponents with BuiltInComponents with NoHttpFiltersComponents {
      override lazy val serverConfig = ServerConfig(port = Some(0))
      override lazy val actorSystem: ActorSystem = ActorSystem("HackerNewsServer")

      override def serverStopHook: () => Future[Unit] = () => actorSystem.terminate().map(_ => Unit)

      override def router: Router = new SimpleRouterImpl(serverRoutes(defaultActionBuilder))
    }

  private def itemsFrom(stories: Seq[APIStory]): Map[Long, JsValue] = {
    stories.flatMap { story =>
      val storyItem = story.id -> Json.obj("id" -> story.id, "title" -> story.title, "kids" -> story.comments.map(_.id), "type" -> "story")
      val commentItems = story.comments.map { comment =>
        comment.id -> Json.obj("id" -> comment.id, "by" -> comment.by, "type" -> "comment")
      }
      storyItem :: commentItems
    }.toMap
  }

}

case class APIStory(id: Long, comments: List[APIComment]) {
  lazy val title = s"Story$id"
}

case class APIComment(id: Long, by: String)
