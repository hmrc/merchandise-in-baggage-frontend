import uk.gov.hmrc.DefaultBuildSettings.itSettings

ThisBuild / scalaVersion := "2.13.15"
ThisBuild / majorVersion := 0

lazy val microservice = Project("merchandise-in-baggage-frontend", file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(CodeCoverageSettings.settings)
  .settings(
    PlayKeys.playDefaultPort := 8281,
    libraryDependencies ++= AppDependencies(),
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.merchandiseinbaggage.config.AppConfig",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.merchandiseinbaggage.views.ViewUtils._"
    ),
    scalacOptions ++= Seq("-Wconf:src=routes/.*:s", "-Wconf:cat=unused-imports&src=html/.*:s", "-feature")
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(itSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt A11y/scalafmt it/Test/scalafmt")
