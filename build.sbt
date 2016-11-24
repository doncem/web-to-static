import sbt.Keys._

lazy val `web-to-static` = (project in file(".")).settings(
  name := "web-to-static",
  version := "1.0",
  scalaVersion := "2.12.0",
  libraryDependencies ++= Seq(
    "ch.qos.logback"             % "logback-classic"  % "1.1.7",
    "com.typesafe.scala-logging" %% "scala-logging"   % "3.5.0"
  ),
  resolvers ++= Seq(
    "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    Classpaths.sbtPluginReleases
  )
).settings(Revolver.settings: _*)
