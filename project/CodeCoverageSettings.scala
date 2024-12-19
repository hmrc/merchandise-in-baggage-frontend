import sbt.Setting
import scoverage.ScoverageKeys.{coverageExcludedFiles, coverageFailOnMinimum, coverageHighlighting, coverageMinimumStmtTotal}

object CodeCoverageSettings {
  private val excludedPackages: Seq[String] = Seq(
    "<empty>",
    ".*components.*",
    ".*Routes.*",
    ".*testonly.*"
  )

  val settings: Seq[Setting[?]] = Seq(
    coverageExcludedFiles := excludedPackages.mkString(";"),
    coverageMinimumStmtTotal := 91,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
  )
}
