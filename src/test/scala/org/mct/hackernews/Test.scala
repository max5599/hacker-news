package org.mct.hackernews

import org.scalatest.{EitherValues, Matchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}

trait Test extends Matchers with ScalaFutures with EitherValues {
  override implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(2, Seconds))
}