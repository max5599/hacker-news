package org.mct.hackernews

trait SimulatedHackerNews {

  def withHackerNews[T](stories: Seq[Story])(block: String => T) : T = ???

}

case class Story(title: String, comments: Seq[Comment])

case class Comment(by: String)
