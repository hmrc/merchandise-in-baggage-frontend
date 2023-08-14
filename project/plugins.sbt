resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

resolvers += Resolver.typesafeRepo("releases")

// To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
// Try to remove when sbt 1.8.0+ and scoverage is 2.0.7+
ThisBuild / libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always)

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.9.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.2.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.20")

addSbtPlugin("org.irundaia.sbt" % "sbt-sassify" % "1.5.1")

addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.16")

addSbtPlugin("com.beautiful-scala" % "sbt-scalastyle" % "1.5.1")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.8")

addSbtPlugin("com.itv" % "sbt-scalapact" % "3.3.1")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")
