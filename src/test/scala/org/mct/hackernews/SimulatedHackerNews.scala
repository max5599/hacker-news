package org.mct.hackernews

import akka.actor.ActorSystem
import play.api.libs.json.{JsString, JsValue, Json}
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.sird._
import play.api.routing.{Router, SimpleRouterImpl}
import play.api.{BuiltInComponents, NoHttpFiltersComponents}
import play.core.server.{AkkaHttpServerComponents, ServerConfig}

import scala.concurrent.{ExecutionContext, Future}

trait SimulatedHackerNews {

  def withHackerNewsAndStories[T](stories: Seq[APIStory])(block: String => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val items: Map[Long, JsValue] = itemsFrom(stories)
    withHackerNewsServer(
      topStoriesResult = Ok(Json.toJson(stories.map(_.id))),
      itemResult = id => Ok(items(id))
    )(block)
  }

  def withHackerNewsServer[T](
                               topStoriesResult: => Result = Ok(Json.arr()),
                               itemResult: Long => Result = _ => NotFound
                             )(block: String => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val hackerNewsServer = createServer {
      action => {
        case GET(p"/v0/topstories.json") => action(topStoriesResult)
        case GET(p"/v0/item/${long(id)}.json") => action(itemResult(id))
      }
    }.server
    block(s"http://localhost:${hackerNewsServer.httpPort.get}")
      .andThen { case _ => hackerNewsServer.stop() }
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
