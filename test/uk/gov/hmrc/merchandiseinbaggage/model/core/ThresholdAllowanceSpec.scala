package uk.gov.hmrc.merchandiseinbaggage.model.core

import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}
import uk.gov.hmrc.merchandiseinbaggage.model.core.ThresholdAllowance._

class ThresholdAllowanceSpec extends BaseSpec with CoreTestData {

  "return threshold allowance left" in {
    aThresholdAllowance.allowanceLeft mustBe 1488L
  }
}
