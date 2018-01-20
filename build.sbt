name := "hacker-news"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(

)

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.play" %% "play-akka-http-server" % "2.6.11",
  "org.scalatest" %% "scalatest" % "3.0.4"
).map(_ % Test)
