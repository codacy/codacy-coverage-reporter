addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.7.6")
addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "20.1.1")

// Publish
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")

// Updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

libraryDependencies += "org.codehaus.plexus" % "plexus-archiver" % "4.2.2"
