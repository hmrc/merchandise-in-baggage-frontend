import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val pactVersion = "3.2.0"

  val compile = Seq(
    "uk.gov.hmrc"                %% "bootstrap-frontend-play-28" % "5.3.0",
    "uk.gov.hmrc"                %% "play-frontend-hmrc"         % "0.69.0-play-28",
    "uk.gov.hmrc"                %% "simple-reactivemongo"       % "8.0.0-play-28",
    "com.github.pureconfig"      %% "pureconfig"                 % "0.13.0",
    "com.beachape"               %% "enumeratum-play"            % "1.5.13",
    "org.webjars.npm"            %  "accessible-autocomplete"    % "2.0.3",
    "uk.gov.hmrc"                %% "play-language"              % "5.1.0-play-28",
    "com.softwaremill.quicklens" %% "quicklens"                  % "1.6.1",
    "org.typelevel"              %% "cats-core"                  % "2.2.0"
  )

  val test = Seq(
    "uk.gov.hmrc"              %% "bootstrap-test-play-28"  % "5.3.0"   % Test,
    "org.scalatest"            %% "scalatest"               % "3.2.9"    % Test,
    "com.typesafe.play"        %% "play-test"               % current    % Test,
    "com.vladsch.flexmark"     %  "flexmark-all"            % "0.35.10"  % "test, it",
    "org.scalatestplus.play"   %% "scalatestplus-play"      % "5.1.0"    % "test, it",
    "org.scalatestplus"        %% "scalacheck-1-14"         % "3.2.2.0"  % Test,
    "org.scalatestplus"        %% "selenium-3-141"          % "3.2.9.0"  % Test
  ) ++ pact ++ mocks

  private lazy val pact = Seq(
    "com.itv"                  %% "scalapact-circe-0-13"    % pactVersion    % Test,
    "com.itv"                  %% "scalapact-http4s-0-21"   % pactVersion    % Test,
    "com.itv"                  %% "scalapact-scalatest"     % pactVersion    % Test,
    "org.scalaj"               %% "scalaj-http"             % "2.4.2"    % Test,
    "org.json4s"               %% "json4s-native"           % "3.6.9"    % Test
  )

  private lazy val mocks = Seq(
    "org.scalamock"            %% "scalamock" % "5.1.0"     % Test,
    "com.github.tomakehurst"   %  "wiremock-standalone"     % "2.27.1"   % Test
  )
}
