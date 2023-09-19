val appName = "merchandise-in-baggage-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalaPactPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion := 0,
    scalaVersion := "2.13.11",
    PlayKeys.playDefaultPort := 8281,
    Test / fork := false,
    libraryDependencies ++= AppDependencies(),
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always),
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.merchandiseinbaggage.config.AppConfig",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.merchandiseinbaggage.views.ViewUtils._"
    ),
    scalacOptions ++= Seq("-Wconf:src=routes/.*:s", "-Wconf:cat=unused-imports&src=html/.*:s")
  )
  .settings(
    coverageExcludedFiles := "<empty>;Reverse.*;.*BuildInfo.*;.*javascript.*;.*Routes.*;.*testonly.*;",
    coverageMinimumStmtTotal := 90,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
  )
  .settings(
    A11yTest / unmanagedSourceDirectories += (baseDirectory.value / "test" / "a11y")
  )

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt A11y/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle A11y/scalastyle")
