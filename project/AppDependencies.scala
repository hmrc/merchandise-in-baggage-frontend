import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val pactVersion = "4.4.0"

  val compile = Seq(
    "uk.gov.hmrc"                %% "bootstrap-frontend-play-28" % "5.24.0",
    "uk.gov.hmrc"                %% "auth-client"                % "5.12.0-play-28",
    "uk.gov.hmrc"                %% "play-frontend-hmrc"         % "6.2.0-play-28",
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-28"         % "0.74.0",
    "com.github.pureconfig"      %% "pureconfig"                 % "0.17.1",
    "com.beachape"               %% "enumeratum-play"            % "1.7.0",
    "org.webjars.npm"            % "accessible-autocomplete"     % "2.0.3",
    "com.softwaremill.quicklens" %% "quicklens"                  % "1.8.3",
    "org.typelevel"              %% "cats-core"                  % "2.7.0"
  )

  val test = (Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28" % "5.24.0",
    "org.scalatest"          %% "scalatest"              % "3.2.15",
    "com.typesafe.play"      %% "play-test"              % current,
    "com.vladsch.flexmark"   % "flexmark-all"            % "0.62.2",
    "org.scalatestplus.play" %% "scalatestplus-play"     % "5.1.0",
    "org.scalatestplus"      %% "scalacheck-1-17"        % "3.2.15.0",
    "org.scalatestplus"      %% "selenium-4-7"           % "3.2.15.0"
  ) ++ pact ++ mocks).map(_ % Test)

  private lazy val pact = Seq(
    "com.itv"    %% "scalapact-circe-0-13"  % pactVersion,
    "com.itv"    %% "scalapact-http4s-0-21" % pactVersion,
    "com.itv"    %% "scalapact-scalatest"   % pactVersion,
    "org.json4s" %% "json4s-native"         % "4.0.6"
  )

  private lazy val mocks = Seq(
    "org.scalamock"          %% "scalamock"          % "5.2.0",
    "com.github.tomakehurst" % "wiremock-standalone" % "2.27.2"
  )
}
