import sbt._

object Dependencies {

  lazy val codacyScalaApi = "com.codacy" %% "codacy-api-scala" % "1.0.2"
  lazy val coverageParser = "com.codacy" %% "coverage-parser" % "1.1.2"
  lazy val scopt = "com.github.scopt" %% "scopt" % "3.3.0"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "2.2.4" % "test"

}
