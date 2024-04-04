import sbt._

object AppDependencies {

  private val pactVersion      = "4.4.0"
  private val bootstrapVersion = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"                %% "play-frontend-hmrc-play-30" % "8.5.0",
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-30"         % "1.8.0",
    "com.github.pureconfig"      %% "pureconfig"                 % "0.17.6",
    "com.beachape"               %% "enumeratum-play"            % "1.8.0",
    "org.webjars.npm"             % "accessible-autocomplete"    % "2.0.4",
    "com.softwaremill.quicklens" %% "quicklens"                  % "1.9.7",
    "org.typelevel"              %% "cats-core"                  % "2.10.0",
    "io.github.samueleresca"     %% "pekko-quartz-scheduler"     % "1.2.0-pekko-1.0.x"
  )

  val test: Seq[ModuleID] = (Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion,
    "org.scalatest"     %% "scalatest"               % "3.2.18",
    "org.scalatestplus" %% "scalacheck-1-17"         % "3.2.18.0",
    "org.mockito"       %% "mockito-scala-scalatest" % "1.17.31",
    "org.wiremock"       % "wiremock-standalone"     % "3.5.2"
  ) ++ pact).map(_ % Test)

  private lazy val pact   = Seq(
    "com.itv"    %% "scalapact-circe-0-13"  % pactVersion,
    "com.itv"    %% "scalapact-http4s-0-21" % pactVersion,
    "com.itv"    %% "scalapact-scalatest"   % pactVersion,
    "org.json4s" %% "json4s-native"         % "4.0.7"
  )

  def apply(): Seq[ModuleID] = compile ++ test

}
