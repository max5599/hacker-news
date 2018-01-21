package org.mct.hackernews

import java.util.concurrent.ForkJoinPool

import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

object RetrieveTopStoriesFromHackerNews extends App with WSClient {

  lazy val logger = LoggerFactory.getLogger(RetrieveTopStoriesFromHackerNews.getClass)
  implicit lazy val ec: ExecutionContext = ExecutionContext.fromExecutor(new ForkJoinPool(20))

  val result = withWSClient { implicit ws =>
    RetrieveTopStories("https://hacker-news.firebaseio.com").apply().value.map {
      case Right(topStories) =>
        logger.info("Here's top stories from hacker-news: {}", topStories)
        0
      case Left(error) =>
        logger.error("Error while getting top stories: {}", error)
        1
    }
  }

  System.exit(Await.result(result, 30.seconds))

}
