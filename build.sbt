lazy val root = project
  .in(file("."))
  .settings(
    scalaVersion := "3.3.3",
    scalacOptions := {
      val toFilter = Set("-deprecation:false")
      scalacOptions.value.filterNot(toFilter) ++ Seq("-deprecation")
    },
    name := "codacy-coverage-reporter",
// Runtime dependencies
    libraryDependencies ++= Seq(
      "com.github.alexarchambault" %% "case-app" % "2.1.0-M28",
      "org.wvlet.airframe" %% "airframe-log" % "24.6.0",
      "com.codacy" %% "codacy-plugins-api" % "8.1.4",
      "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
      "com.typesafe.play" %% "play-json" % "2.10.5",
      ("org.scalaj" %% "scalaj-http" % "2.4.2").cross(CrossVersion.for3Use2_13),
      "org.eclipse.jgit" % "org.eclipse.jgit" % "4.11.9.201909030838-r"
    ),
// Test dependencies
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.18" % "it,test",
      "org.scalamock" %% "scalamock" % "6.0.0" % Test
    ),
    assembly / mainClass := Some("com.codacy.CodacyCoverageReporter"),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
      case m if m.toLowerCase.matches("meta-inf.*\\.sf$") => MergeStrategy.discard
      case _ => MergeStrategy.first
    },
    assembly / test := {},
    crossPaths := false,
    run / fork := true,
// HACK: Since we are only using the public resolvers we need to remove the private for it to not fail
    resolvers ~= {
      _.filterNot(_.name.toLowerCase.contains("codacy"))
    },
    description := "CLI to send coverage reports to Codacy through the API",
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/codacy/codacy-coverage-reporter"),
        "scm:git:git@github.com:codacy/codacy-coverage-reporter.git"
      )
    ),
    Test / fork := true,
    Global / cancelable := true,
    javacOptions ++= Seq("-source", "11", "-target", "11"),
    nativeImageVersion := "22.3.0", {
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
    },
    // https://github.com/sbt/sbt-assembly/issues/146
    assemblyMergeStrategy := {
      case PathList("module-info.class") => MergeStrategy.discard
      case x if x.endsWith("/module-info.class") => MergeStrategy.discard
      case x =>
        val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )
  .enablePlugins(NativeImagePlugin)
