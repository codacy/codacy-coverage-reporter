resolvers := Seq(DefaultMavenRepository, Resolver.jcenterRepo, Resolver.sonatypeRepo("releases"))

addSbtPlugin("com.codacy" % "codacy-sbt-plugin" % "14.0.1")

// Publish
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")

// Updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.2")
