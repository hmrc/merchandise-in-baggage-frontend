val appName = "merchandise-in-baggage-frontend"

ThisBuild / scalaVersion := "2.13.13"
ThisBuild / majorVersion := 0

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalaPactPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(CodeCoverageSettings.settings)
  .settings(
    // this scala-xml version scheme is to get around some library dependency conflicts
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    PlayKeys.playDefaultPort := 8281,
    Test / fork := false,
    libraryDependencies ++= AppDependencies(),
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.merchandiseinbaggage.config.AppConfig",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.merchandiseinbaggage.views.ViewUtils._"
    ),
    scalacOptions ++= Seq("-Wconf:src=routes/.*:s", "-Wconf:cat=unused-imports&src=html/.*:s")
  )

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt A11y/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle A11y/scalastyle")
