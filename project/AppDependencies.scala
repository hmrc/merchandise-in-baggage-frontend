import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.0.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"                %% "play-frontend-hmrc-play-30" % "10.4.0",
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-30"         % "2.1.0",
    // need to match playframework version from plugin and bootstrap and this library in this case now it is 3.0.3 everywhere
    "com.beachape"               %% "enumeratum-play"            % "1.8.0",
    "org.webjars.npm"             % "accessible-autocomplete"    % "2.0.4",
    "com.softwaremill.quicklens" %% "quicklens"                  % "1.9.7",
    "org.typelevel"              %% "cats-core"                  % "2.12.0",
    "io.github.samueleresca"     %% "pekko-quartz-scheduler"     % "1.2.1-pekko-1.0.x"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.scalatestplus" %% "scalacheck-1-18"        % "3.2.19.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID]      = compile ++ test

}
