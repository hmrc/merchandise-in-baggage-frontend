/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs

import com.softwaremill.macwire.wire
import uk.gov.hmrc.merchandiseinbaggagefrontend.CoreTestData
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationJourney, YesNo}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.CustomsAgentPage._
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.{AgentDetailsPage, EoriNumberPage, RadioButtonPage}

class CustomsAgentPageSpec extends DeclarationDataCapturePageSpec[YesNo, RadioButtonPage[YesNo]] with CoreTestData {
  override lazy val page: RadioButtonPage[YesNo] = wire[RadioButtonPage[YesNo]]


  "the page" should {
    behave like aPageWhichRequiresADeclarationJourney(path)
    behave like aDataCapturePageWithConditionalRouting(path, setup(), Yes, AgentDetailsPage.path)
    behave like aDataCapturePageWithConditionalRouting(path, setup(), No, EoriNumberPage.path)

    "render correctly" when {
      "the declaration has missing customs agent answer" in {
        givenADeclarationJourney(completedDeclarationJourney.copy(maybeIsACustomsAgent = None))
        open(path)

        page.mustRenderBasicContent(path, title)
      }
    }
  }

  private def setup(): Unit =
    givenADeclarationJourney(startedImportJourney.copy(maybeIsACustomsAgent = Some(Yes)))

  override def extractFormDataFrom(declarationJourney: DeclarationJourney): Option[YesNo] =
    declarationJourney.maybeIsACustomsAgent
}
