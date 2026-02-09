import sbt.*

object AppDependencies {

  private val bootstrapVersion = "10.5.0"

  private val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"                %% "play-frontend-hmrc-play-30" % "12.31.0",
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-30"         % "2.12.0",
    "com.beachape"               %% "enumeratum-play"            % "1.9.4",
    "org.webjars.npm"             % "accessible-autocomplete"    % "3.0.1",
    "com.softwaremill.quicklens" %% "quicklens"                  % "1.9.12",
    "org.typelevel"              %% "cats-core"                  % "2.13.0",
    "io.github.samueleresca"     %% "pekko-quartz-scheduler"     % "1.2.2-pekko-1.0.x"
  )

  private val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.scalatestplus" %% "scalacheck-1-18"        % "3.2.19.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
