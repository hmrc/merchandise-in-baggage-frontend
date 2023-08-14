import play.sbt.PlayImport.PlayKeys.playDefaultPort
import scoverage.ScoverageKeys

val appName = "merchandise-in-baggage-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, ScalaPactPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.11",
    playDefaultPort                  := 8281,
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always),
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.merchandiseinbaggage.config.AppConfig",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.merchandiseinbaggage.views.ViewUtils._",
    ),
    scalacOptions ++= Seq("-Wconf:src=routes/.*:s", "-Wconf:cat=unused-imports&src=html/.*:s")
    // ***************
  )
  .settings(
    retrieveManaged := true,
    Compile / scalafmtOnCompile := true,
    Test / scalafmtOnCompile := true,
    Test / fork := false,
    Test / parallelExecution := false
  )
  .settings(
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*BuildInfo.*;.*javascript.*;.*Routes.*;.*testonly.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
  )

addCommandAlias("scalastyleAll", "all scalastyle test:scalastyle")
