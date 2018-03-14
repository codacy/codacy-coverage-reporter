import sbt._

object Dependencies {

  lazy val codacyScalaApi = "com.codacy" %% "codacy-api-scala" % "3.0.7"
  lazy val coverageParser = "com.codacy" %% "coverage-parser" % "2.0.7"
  lazy val caseApp = "com.github.alexarchambault" %% "case-app" % "1.2.0"
  lazy val raptureJsonPlay = "com.propensive" %% "rapture-json-play" % "2.0.0-M8"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"

  lazy val cats = "org.typelevel" %% "cats-core" % "1.0.1"

  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val log4s = "org.log4s" %% "log4s" % "1.5.0"
}
