addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.4")
addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "25.2.2")

// Publish
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.0")

// Coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.2.2")

libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)
