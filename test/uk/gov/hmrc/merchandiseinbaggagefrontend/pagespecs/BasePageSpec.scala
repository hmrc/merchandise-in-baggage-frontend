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
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpecWithApplication, WireMockSupport}
import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages._

trait BasePageSpec extends BaseSpecWithApplication with WireMockSupport {
  implicit lazy val webDriver: HtmlUnitDriver = new HtmlUnitDriver(false)

  lazy val baseUrl: BaseUrl = BaseUrl(s"http://localhost:$port")

  lazy val testOnlyDeclarationJourneyPage: TestOnlyDeclarationJourneyPage = wire[TestOnlyDeclarationJourneyPage]
  lazy val startImportPage: StartImportPage = wire[StartImportPage]
  lazy val exciseAndRestrictedGoodsPage: ExciseAndRestrictedGoodsPage = wire[ExciseAndRestrictedGoodsPage]
  lazy val agentDetailsPage: AgentDetailsPage = wire[AgentDetailsPage]
  lazy val eoriNumberPage: EoriNumberPage = wire[EoriNumberPage]
  lazy val checkYourAnswersPage: CheckYourAnswersPage = wire[CheckYourAnswersPage]

  def startImportJourney(): Unit = {
    startImportPage.open()
    startImportPage.clickOnStartNowButton()
  }
}

