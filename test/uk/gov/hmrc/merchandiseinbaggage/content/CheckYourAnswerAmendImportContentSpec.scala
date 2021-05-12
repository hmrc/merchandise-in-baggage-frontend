/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.merchandiseinbaggage.content

import org.openqa.selenium.{By, WebElement}
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResult, ThresholdCheck, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.CheckYourAnswersPage
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._

import scala.collection.JavaConverters._

class CheckYourAnswerAmendImportContentSpec extends CheckYourAnswersPage with CoreTestData {

  "render proof of origin needed if good EU origin goods amount is > 1000" in {
    setUp(aCalculationResultOverThousand, WithinThreshold) { bulletPoints =>
      bulletPoints.size mustBe 4
      elementText(bulletPoints(0)) mustBe s"${messages("checkYourAnswers.amend.sendDeclaration.acknowledgement.1")}"
      elementText(bulletPoints(1)) mustBe s"${messages("checkYourAnswers.sendDeclaration.acknowledgement.EU.over.thousand")}"
      elementText(bulletPoints(2)) mustBe s"${messages("checkYourAnswers.sendDeclaration.Import.acknowledgement.1")}"
      elementText(bulletPoints(3)) mustBe s"${messages("checkYourAnswers.sendDeclaration.Import.acknowledgement.2")}"
      elementText(findByTagName("button")) mustBe s"${messages("checkYourAnswers.payButton")}"
    }
  }

  "do not render proof of origin needed if good EU origin goods amount is < 1000" in {
    setUp(aCalculationResult) { bulletPoints =>
      bulletPoints.size mustBe 3
      elementText(bulletPoints(0)) mustBe s"${messages("checkYourAnswers.amend.sendDeclaration.acknowledgement.1")}"
      elementText(bulletPoints(1)) mustBe s"${messages("checkYourAnswers.sendDeclaration.Import.acknowledgement.1")}"
      elementText(bulletPoints(2)) mustBe s"${messages("checkYourAnswers.sendDeclaration.Import.acknowledgement.2")}"
      elementText(findByTagName("button")) mustBe s"${messages("checkYourAnswers.payButton")}"
    }
  }

  s"render different button with text ${messages("checkYourAnswers.makeDeclarationButton")} when nothing to pay" in {
    setUp(aCalculationResultWithNothingToPay) { _ =>
      elementText(findByTagName("button")) mustBe s"${messages("checkYourAnswers.makeDeclarationButton")}"
    }
  }

  private def setUp(calculationResult: CalculationResult, thresholdCheck: ThresholdCheck = WithinThreshold)(
    fn: List[WebElement] => Any): Any = fn {
    givenAnAmendPaymentCalculations(Seq(calculationResult), thresholdCheck)
    val journey = completedAmendedJourney(Import).copy(declarationId = aDeclarationId, maybeGoodsDestination = Some(GreatBritain))
    givenAJourneyWithSession(Amend, declarationJourney = journey)
    goToCYAPage(Amend)

    findByXPath("//ul[@name='declarationAcknowledgement']")
      .findElements(By.tagName("li"))
      .asScala
      .toList
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    declarationJourneyRepository.deleteAll()
  }
}
