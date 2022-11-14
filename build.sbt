inThisBuild(
  Seq(
    scalaVersion := "2.12.11",
    scalacOptions := Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Ywarn-adapted-args",
      "-Xlint",
      "-Xfatal-warnings",
      "-Ypartial-unification"
    )
  )
)

name := "codacy-coverage-reporter"

// Runtime dependencies
libraryDependencies ++= Seq(
  "com.github.alexarchambault" %% "case-app" % "1.2.0",
  "org.wvlet.airframe" %% "airframe-log" % "22.3.0",
  "com.lihaoyi" %% "ujson" % "1.5.0"
)

// Test dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.8" % "it,test",
  "org.mockito" %% "mockito-scala-scalatest" % "1.7.1" % Test
)

configs(IntegrationTest)
Defaults.itSettings

assembly / mainClass := Some("com.codacy.CodacyCoverageReporter")
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case _ => MergeStrategy.first
}
assembly / test := {}
crossPaths := false

// HACK: Since we are only using the public resolvers we need to remove the private for it to not fail
resolvers ~= {
  _.filterNot(_.name.toLowerCase.contains("codacy"))
}

description := "CLI to send coverage reports to Codacy through the API"

scmInfo := Some(
  ScmInfo(
    url("https://github.com/codacy/codacy-coverage-reporter"),
    "scm:git:git@github.com:codacy/codacy-coverage-reporter.git"
  )
)

Test / fork := true
Global / cancelable := true

javacOptions ++= Seq("-source", "11", "-target", "11")

enablePlugins(GraalVMNativeImagePlugin)
GraalVMNativeImage / containerBuildImage := Some("graavlm:latest")

graalVMNativeImageOptions := Seq(
  "--verbose",
  "--no-server",
  "--enable-http",
  "--enable-https",
  "--enable-url-protocols=http,https,jar",
  "--enable-all-security-services",
  "-H:+JNI",
  "-H:IncludeResourceBundles=com.sun.org.apache.xerces.internal.impl.msg.XMLMessages",
  "-H:+ReportExceptionStackTraces",
  "--no-fallback",
  "--initialize-at-build-time",
  "--report-unsupported-elements-at-runtime",
  "--static",
  "--libc=musl"
)

dependsOn(coverageParser)

lazy val coverageParser = project
  .in(file("coverage-parser"))
  .settings(
    libraryDependencies ++= Seq(
      "com.codacy" %% "codacy-api-scala" % "7.0.3",
      "com.codacy" %% "codacy-plugins-api" % "5.2.0",
      "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
      "org.scalatest" %% "scalatest" % "3.0.8" % Test
    )
  )

// https://github.com/sbt/sbt-assembly/issues/146
ThisBuild / assemblyMergeStrategy := {
  case PathList("module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}
