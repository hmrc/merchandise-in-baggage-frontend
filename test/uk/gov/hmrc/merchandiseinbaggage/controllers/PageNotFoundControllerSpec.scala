/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.views.html.PageNotFoundView

import scala.concurrent.ExecutionContext.Implicits.global

class PageNotFoundControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  private val view = app.injector.instanceOf[PageNotFoundView]

  def controller: PageNotFoundController =
    new PageNotFoundController(controllerComponents, stubProvider(startedImportJourney), view)

  declarationTypes.foreach { importOrExport: DeclarationType =>
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request        = buildGet(routes.PageNotFoundController.onPageLoad.url, aSessionId)
        val eventualResult = controller.onPageLoad()(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe OK
        result must include(messages(s"pageNotFound.li1"))
        result must include(messages(s"pageNotFound.li2"))
        result must include(messages(s"pageNotFound.li3"))
        result must include(messages(s"pageNotFound.$importOrExport.restart"))
      }
    }
  }
}
