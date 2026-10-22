inThisBuild(Seq(scalaVersion := "3.3.7"))

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

nativeImageJvm := "graalvm-java25"

nativeImageVersion := "25.0.2"

val osSpecificOptions =
  (Platform.os, Platform.arch) match {
    case (Platform.OS.Linux, Platform.Arch.Intel) => Seq("--static", "--libc=musl")
    case (Platform.OS.Linux, Platform.Arch.Arm) => Seq("--static")
    case _ => Seq.empty[String]
  }

// native-image embeds the build JDK's cacerts. Until a GraalVM with JDK >= 25.0.5 is available,
// remove the distrusted AAA Certificate Services root from it before building.
val stripDistrustedCerts = taskKey[Unit]("Remove AAA Certificate Services from the GraalVM cacerts")

stripDistrustedCerts := {
  val cacerts = nativeImageGraalHome.value.resolve("lib/security/cacerts").toFile
  val script = (ThisBuild / baseDirectory).value / "graalvm" / "strip-aaa-cacerts.sh"
  val tmp = target.value / "trust" / "cacerts"
  val exit = scala.sys.process.Process(Seq(script.getAbsolutePath, tmp.getAbsolutePath), None, "JAVA_HOME" -> nativeImageGraalHome.value.toString).!
  if (exit != 0) sys.error("failed to strip distrusted certificates")
  java.nio.file.Files.copy(tmp.toPath, cacerts.toPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
}

nativeImage := nativeImage.dependsOn(stripDistrustedCerts).value

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

val scalatest = "org.scalatest" %% "scalatest" % "3.2.19"

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
      "com.codacy" %% "codacy-plugins-api" % "12.0.0",
      "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
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
