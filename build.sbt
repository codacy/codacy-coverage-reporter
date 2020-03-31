import codacy.libs._

name := "codacy-coverage-reporter"

scalaVersion := "2.12.10"

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
  "com.codacy" %% "coverage-parser" % "2.5.6",
  "com.github.alexarchambault" %% "case-app" % "1.2.0",
  logbackClassic,
  scalaLogging
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

enablePlugins(GraalVMNativeImagePlugin)
graalVMNativeImageGraalVersion := Some("20.0.0-java8")
graalVMNativeImageOptions := Seq(
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
  "--report-unsupported-elements-at-runtime"
)
