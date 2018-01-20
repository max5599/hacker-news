organization := "org.mct"
name := "hacker-news"
version := "0.1"

scalaVersion := "2.12.4"
scalacOptions += "-Ypartial-unification"

lazy val playWsStandaloneVersion = "1.1.3"
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-ahc-ws-standalone" % playWsStandaloneVersion,
  "com.typesafe.play" %% "play-ws-standalone-json" % playWsStandaloneVersion,
  "org.typelevel" %% "cats-core" % "1.0.1"
)

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.play" %% "play-akka-http-server" % "2.6.11",
  "org.scalatest" %% "scalatest" % "3.0.4"
).map(_ % Test)
