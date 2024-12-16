import sbt.Keys.scalacOptions
import uk.gov.hmrc.DefaultBuildSettings.itSettings

import scala.collection.Seq

ThisBuild / scalaVersion := "3.5.1"
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
      "uk.gov.hmrc.govukfrontend.views.html.components.*",
      "uk.gov.hmrc.merchandiseinbaggage.views.ViewUtils.*"
    ),
    scalacOptions ++= List("-Wconf:src=routes/.*:s", "-Wconf:msg=unused import&src=html/.*:s"),
    scalacOptions ++= List("-rewrite", "-source", "3.3-migration")
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(itSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt it/Test/scalafmt")
