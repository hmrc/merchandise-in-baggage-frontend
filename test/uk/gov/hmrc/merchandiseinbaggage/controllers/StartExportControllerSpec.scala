/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.test.Helpers.{status, _}
import play.mvc.Http.Status
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney

import scala.concurrent.ExecutionContext.Implicits.global

class StartExportControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  def controller(declarationJourney: DeclarationJourney = startedExportJourney): StartExportController =
    new StartExportController(controllerComponents, stubRepo(declarationJourney))

  "onPageLoad" should {
    "store redirect" in {
      val url     = routes.StartExportController.onPageLoad.url
      val request = buildGet(url, aSessionId)
      val result  = controller().onPageLoad()(request)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.StartExportController.processRequest.url)
    }
  }

  "process-request" should {
    "store the declaration type in mongo" in {
      val url     = routes.StartExportController.processRequest.url
      val request = buildPost(url, aSessionId)
      val result  = controller().processRequest()(request)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.NewOrExistingController.onPageLoad.url)
    }
  }
}
