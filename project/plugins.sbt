addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.4")
addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "25.1.1")

// Publish
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.4")

// Coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.11")

libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)
