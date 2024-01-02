inThisBuild(
  Seq(
    scalaVersion := "2.12.18",
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

assembly / mainClass := Some("com.codacy.CodacyCoverageReporter")
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case _ => MergeStrategy.first
}
assembly / test := {}
crossPaths := false
run / fork := true

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

enablePlugins(NativeImagePlugin)

nativeImageVersion := "22.3.0"

val osSpecificOptions =
  if (sys.props("os.name") == "Mac OS X") Seq.empty[String]
  else Seq("--static", "--libc=musl")

nativeImageOptions := Seq(
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
  "--report-unsupported-elements-at-runtime"
) ++ osSpecificOptions

dependsOn(coverageParser)

lazy val apiScala = project
  .in(file("api-scala"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.8.2",
      "org.scalaj" %% "scalaj-http" % "2.4.2",
      "org.eclipse.jgit" % "org.eclipse.jgit" % "4.11.9.201909030838-r",
      "org.scalatest" %% "scalatest" % "3.0.8" % Test
    )
  )

lazy val coverageParser = project
  .in(file("coverage-parser"))
  .settings(
    libraryDependencies ++= Seq(
      "com.codacy" %% "codacy-plugins-api" % "5.2.0",
      "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
      "org.scalatest" %% "scalatest" % "3.0.8" % Test
    )
  )
  .dependsOn(apiScala)

// https://github.com/sbt/sbt-assembly/issues/146
ThisBuild / assemblyMergeStrategy := {
  case PathList("module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

// required to upgrade org.scoverage.sbt-scoverage.2.0.6
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
