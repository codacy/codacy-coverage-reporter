import Dependencies._

name := "codacy-coverage-reporter"

organization := "com.codacy"

scalaVersion := "2.11.12"

scalacOptions := Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Ywarn-adapted-args",
  "-Xlint",
  "-Xfatal-warnings",
  "-Ypartial-unification"
)

libraryDependencies ++= Seq(coverageParser, logback, log4s, caseApp, raptureJsonPlay, scalaTest, cats, javaxActivation)

mainClass in assembly := Some("com.codacy.CodacyCoverageReporter")
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
  case _ => MergeStrategy.first
}
crossPaths := false
artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.copy(`classifier` = Some("assembly"))
}
addArtifact(artifact in (Compile, assembly), assembly)

organizationName := "Codacy"
organizationHomepage := Some(new URL("https://www.codacy.com"))

// this setting is not picked up properly from the plugin
pgpPassphrase := Option(System.getenv("SONATYPE_GPG_PASSPHRASE")).map(_.toCharArray)

resolvers ~= { _.filterNot(_.name.toLowerCase.contains("codacy")) }

publicMvnPublish

startYear := Some(2015)
description := "Library for parsing coverage reports"
licenses := Seq("The Apache Software License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
homepage := Some(url("http://www.github.com/codacy/codacy-coverage-reporter/"))
pomExtra :=
  <scm>
    <url>https://github.com/codacy/codacy-coverage-reporter</url>
    <connection>scm:git:git@github.com:codacy/codacy-coverage-reporter.git</connection>
    <developerConnection>scm:git:https://github.com/codacy/codacy-coverage-reporter.git</developerConnection>
  </scm>
    <developers>
      <developer>
        <id>mrfyda</id>
        <name>Rafael</name>
        <email>rafael [at] codacy.com</email>
        <url>https://github.com/mrfyda</url>
      </developer>
    </developers>

fork in Test := true
cancelable in Global := true
