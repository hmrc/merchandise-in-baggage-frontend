import uk.gov.hmrc.DefaultBuildSettings.itSettings

val appName = "merchandise-in-baggage-frontend"

ThisBuild / scalaVersion := "2.13.13"
ThisBuild / majorVersion := 0

lazy val microservice = Project(appName, file("."))
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
    scalacOptions ++= Seq("-Wconf:src=routes/.*:s", "-Wconf:cat=unused-imports&src=html/.*:s")
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(itSettings())
  .settings(libraryDependencies ++= AppDependencies.itDependencies)
  .settings(scalacOptionsSettings)

lazy val scalacOptionsSettings: Seq[Setting[?]] = Seq(
  scalacOptions ++= Seq(
    "-Wconf:src=routes/.*:s",
    "-Wconf:cat=unused-imports&src=views/.*:s"
  ),
  scalacOptions ~= { opts =>
    opts.filterNot(
      Set(
        "-Xfatal-warnings",
        "-Ywarn-value-discard"
      )
    )
  }
)

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt A11y/scalafmt it/Test/scalafmt")
addCommandAlias("scalastyleAll", "all scalastyle Test/scalastyle A11y/scalastyle it/Test/scalastyle")
