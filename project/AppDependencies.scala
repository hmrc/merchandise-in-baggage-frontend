import sbt.*

object AppDependencies {

  private val pactVersion      = "4.4.0"
  private val bootstrapVersion = "7.23.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc"                %% "play-frontend-hmrc"         % "7.27.0-play-28",
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-28"         % "1.3.0",
    "com.github.pureconfig"      %% "pureconfig"                 % "0.17.4",
    "com.beachape"               %% "enumeratum-play"            % "1.7.0",
    "org.webjars.npm"             % "accessible-autocomplete"    % "2.0.4",
    "com.softwaremill.quicklens" %% "quicklens"                  % "1.9.6",
    "org.typelevel"              %% "cats-core"                  % "2.10.0",
    "com.enragedginger"          %% "akka-quartz-scheduler"      % "1.9.3-akka-2.6.x"
  )

  val test: Seq[ModuleID] = (Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-28"  % bootstrapVersion,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8",
    "org.scalatest"       %% "scalatest"               % "3.2.17",
    "org.scalatestplus"   %% "scalacheck-1-17"         % "3.2.17.0",
    "org.mockito"         %% "mockito-scala-scalatest" % "1.17.29",
    "org.wiremock"         % "wiremock-standalone"     % "3.3.1"
  ) ++ pact).map(_ % Test)

  private lazy val pact   = Seq(
    "com.itv"    %% "scalapact-circe-0-13"  % pactVersion,
    "com.itv"    %% "scalapact-http4s-0-21" % pactVersion,
    "com.itv"    %% "scalapact-scalatest"   % pactVersion,
    "org.json4s" %% "json4s-native"         % "4.0.6"
  )

  def apply(): Seq[ModuleID] = compile ++ test

}
