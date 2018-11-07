import sbt._

object Dependencies {

  val codacyScalaApi = "com.codacy" %% "codacy-api-scala" % "3.0.7"
  val coverageParser = "com.codacy" %% "coverage-parser" % "2.0.10"
  val caseApp = "com.github.alexarchambault" %% "case-app" % "1.2.0"
  val raptureJsonPlay = "com.propensive" %% "rapture-json-play" % "2.0.0-M8"
  val javaxActivation = "com.sun.activation" % "javax.activation" % "1.2.0"

  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % "test"

  val cats = "org.typelevel" %% "cats-core" % "1.0.1"

  val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  val log4s = "org.log4s" %% "log4s" % "1.5.0"

}
