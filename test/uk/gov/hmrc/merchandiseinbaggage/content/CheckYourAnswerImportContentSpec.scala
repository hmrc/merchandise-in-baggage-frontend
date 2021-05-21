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
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResult
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.smoketests.pages.CheckYourAnswersPage
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenAPaymentCalculation

import scala.collection.JavaConverters._

class CheckYourAnswerImportContentSpec extends CheckYourAnswersPage with CoreTestData {

  "render proof of origin needed if good EU origin goods amount is > 1000" in {
    setUp(aCalculationResultOverThousand, completedDeclarationJourney.copy(maybeIsACustomsAgent = Some(No))) { bulletPoints =>
      bulletPoints.size mustBe 3
      elementText(bulletPoints(0)) mustBe s"${messages("checkYourAnswers.sendDeclaration.acknowledgement.EU.over.thousand")}"
      elementText(bulletPoints(1)) mustBe s"${messages("checkYourAnswers.sendDeclaration.Import.trader.acknowledgement.1")}"
      elementText(bulletPoints(2)) mustBe s"${messages("checkYourAnswers.sendDeclaration.Import.trader.acknowledgement.2")}"
      elementText(findByTagName("button")) mustBe s"${messages("checkYourAnswers.payButton")}"
    }
  }

  "render proof of origin needed if good EU origin goods amount is > 1000 for CustomsAgent" in {
    setUp(aCalculationResultOverThousand, completedDeclarationJourney.copy(maybeIsACustomsAgent = Some(Yes))) { bulletPoints =>
      bulletPoints.size mustBe 3
      elementText(bulletPoints(1)) mustBe s"${messages("checkYourAnswers.sendDeclaration.Import.agent.acknowledgement.1")}"
      elementText(bulletPoints(2)) mustBe s"${messages("checkYourAnswers.sendDeclaration.Import.agent.acknowledgement.2")}"
    }
  }

  "do not render proof of origin needed if good EU origin goods amount is < 1000" in {
    setUp(aCalculationResult, completedDeclarationJourney.copy(maybeIsACustomsAgent = Some(No))) { bulletPoints =>
      bulletPoints.size mustBe 2
      elementText(bulletPoints(0)) mustBe s"${messages("checkYourAnswers.sendDeclaration.Import.trader.acknowledgement.1")}"
      elementText(bulletPoints(1)) mustBe s"${messages("checkYourAnswers.sendDeclaration.Import.trader.acknowledgement.2")}"
      elementText(findByTagName("button")) mustBe s"${messages("checkYourAnswers.payButton")}"
    }
  }

  s"render different button with text ${messages("checkYourAnswers.makeDeclarationButton")} when nothing to pay" in {
    setUp(aCalculationResultWithNothingToPay) { _ =>
      elementText(findByTagName("button")) mustBe s"${messages("checkYourAnswers.makeDeclarationButton")}"
    }
  }

  private def setUp(calculationResult: CalculationResult, journey: DeclarationJourney = completedDeclarationJourney)(
    fn: List[WebElement] => Any): Any = fn {
    givenAPaymentCalculation(calculationResult)
    givenAJourneyWithSession(declarationJourney = journey)
    goToCYAPage()

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
