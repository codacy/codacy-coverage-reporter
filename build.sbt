import java.net.URL

import codacy.libs._

name := "codacy-coverage-reporter"

scalaVersion := "2.12.11"

scalacOptions := Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Ywarn-adapted-args",
  "-Xlint",
  "-Xfatal-warnings",
  "-Ypartial-unification"
)

// Runtime dependencies
libraryDependencies ++= Seq(
  "com.codacy" %% "coverage-parser" % "4.4.1",
  "com.github.alexarchambault" %% "case-app" % "1.2.0",
  "org.wvlet.airframe" %% "airframe-log" % "20.5.1",
  "com.lihaoyi" %% "ujson" % "1.1.0"
)

// Test dependencies
libraryDependencies ++= Seq(scalatest % "it,test", mockitoScalaScalatest % Test)

configs(IntegrationTest)
Defaults.itSettings

mainClass in assembly := Some("com.codacy.CodacyCoverageReporter")
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case _ => MergeStrategy.first
}
test in assembly := {}
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

fork in Test := true
cancelable in Global := true

javacOptions ++= Seq("-source", "11", "-target", "11")

enablePlugins(GraalVMNativeImagePlugin)
graalVMNativeImageGraalVersion := Some("20.0.0-java11")

graalVMNativeImageOptions := Seq(
  "--verbose",
  "--no-server",
  "--enable-http",
  "--enable-https",
  "--enable-url-protocols=http,https,file,jar",
  "--enable-all-security-services",
  "-H:+JNI",
  "--static",
  "-H:IncludeResourceBundles=com.sun.org.apache.xerces.internal.impl.msg.XMLMessages",
  "-H:+ReportExceptionStackTraces",
  "--no-fallback",
  "--initialize-at-build-time",
  "--report-unsupported-elements-at-runtime",
  "-H:UseMuslC=/opt/graalvm/stage/resources/bundle/"
)

val getMuslBundle = taskKey[Unit]("Fetch Musl bundle")

getMuslBundle := {
  if (!(baseDirectory.value / "src" / "graal" / "bundle").exists) {
    TarDownloader.downloadAndExtract(
      new URL("https://github.com/gradinac/musl-bundle-example/releases/download/v1.0/musl.tar.gz"),
      baseDirectory.value / "src" / "graal"
    )
  }
}

GraalVMNativeImage / packageBin := (GraalVMNativeImage / packageBin).dependsOn(getMuslBundle).value
