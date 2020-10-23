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

import uk.gov.hmrc.merchandiseinbaggagefrontend.pagespecs.pages.StartPage

import scala.concurrent.ExecutionContext.Implicits.global

trait StartPageSpec[P <: StartPage] extends BasePageSpec[P]{
  s"${page.path}" should {
    behave like aPageWithSimpleRendering()

    s"allow the user to set up a declaration and redirect to ${page.nextPagePath}" in {
      declarationJourneyRepository.findAll().futureValue.size mustBe 0

      page.open()
      page.clickOnCTA() mustBe page.nextPagePath

      declarationJourneyRepository.findAll().futureValue.size mustBe 1
    }
  }
}
