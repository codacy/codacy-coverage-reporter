inThisBuild(Seq(scalaVersion := "3.3.3"))

def commonSettings =
  Seq(scalacOptions := {
    val toFilter = Set("-deprecation:false")
    scalacOptions.value.filterNot(toFilter) ++ Seq("-deprecation")
  })

name := "codacy-coverage-reporter"

// Runtime dependencies
libraryDependencies ++= Seq(
  "com.github.alexarchambault" %% "case-app" % "2.1.0-M28",
  "org.wvlet.airframe" %% "airframe-log" % "24.6.0"
)

// Test dependencies
libraryDependencies ++= Seq(scalatest % "it,test", "org.scalamock" %% "scalamock" % "6.0.0" % Test)

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
  (Platform.os, Platform.arch) match {
    case (Platform.OS.Linux, Platform.Arch.Intel) => Seq("--static", "--libc=musl")
    case (Platform.OS.Linux, Platform.Arch.Arm) => Seq("--static")
    case _ => Seq.empty[String]
  }

nativeImageOptions := Seq(
  "--verbose",
  "--no-server",
  "--enable-http",
  "--enable-https",
  "--enable-url-protocols=http,https,jar",
  "--enable-all-security-services",
  "-H:+JNI",
  "-H:-CheckToolchain",
  "-H:IncludeResourceBundles=com.sun.org.apache.xerces.internal.impl.msg.XMLMessages",
  "-H:+ReportExceptionStackTraces",
  "--no-fallback",
  "--report-unsupported-elements-at-runtime"
) ++ osSpecificOptions

dependsOn(coverageParser)

commonSettings

val scalatest = "org.scalatest" %% "scalatest" % "3.2.18"

lazy val apiScala = project
  .in(file("api-scala"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.10.5",
      ("org.scalaj" %% "scalaj-http" % "2.4.2").cross(CrossVersion.for3Use2_13),
      "org.eclipse.jgit" % "org.eclipse.jgit" % "4.11.9.201909030838-r",
      scalatest % Test
    )
  )

lazy val coverageParser = project
  .in(file("coverage-parser"))
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "com.codacy" %% "codacy-plugins-api" % "8.1.4",
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
      scalatest % Test
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
