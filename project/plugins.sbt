resolvers := Seq(DefaultMavenRepository, Resolver.jcenterRepo, Resolver.sonatypeRepo("releases"))

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.6.1")
addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "18.0.3")

// Publish
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")

// Updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
