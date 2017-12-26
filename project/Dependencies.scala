import sbt._

object Dependencies {

  lazy val codacyScalaApi = "com.codacy" %% "codacy-api-scala" % "3.0.3"
  lazy val coverageParser = "com.codacy" %% "coverage-parser" % "2.0.1"
  lazy val scopt = "com.github.scopt" %% "scopt" % "3.3.0"
  lazy val log = "ch.qos.logback" % "logback-classic" % "1.2.1"
  lazy val raptureJsonPlay = "com.propensive" %% "rapture-json-play" % "2.0.0-M7"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % "test" 


}
