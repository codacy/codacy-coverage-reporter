addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.4")
addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "25.1.1")

// Publish
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.4")

// Updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")

// Coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.8")
evictionErrorLevel := Level.Warn
