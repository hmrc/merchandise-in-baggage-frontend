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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.merchandiseinbaggage.config.AmendFlagConf
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.NewOrExistingView

import scala.concurrent.ExecutionContext.Implicits.global

class NewOrExistingControllerSpec extends DeclarationJourneyControllerSpec {

  private val view = injector.instanceOf[NewOrExistingView]

  def controller(declarationJourney: DeclarationJourney, amendFlag: Boolean = true) =
    new NewOrExistingController(controllerComponents, stubProvider(declarationJourney), stubRepo(declarationJourney), view) {
      override lazy val amendFlagConf: AmendFlagConf = AmendFlagConf(amendFlag)
    }

  declarationTypes.foreach { importOrExport: DeclarationType =>
    val journey: DeclarationJourney = DeclarationJourney(aSessionId, importOrExport)
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request = buildGet(routes.NewOrExistingController.onPageLoad().url, aSessionId)
        val eventualResult = controller(journey).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi(s"newOrExisting.title"))
        result must include(messageApi(s"newOrExisting.heading"))

        result must include(messageApi(s"service.name.${importOrExport.entryName}.a.href"))
      }

      s"redirect to ${routes.GoodsDestinationController.onPageLoad().url} if flag is false for $importOrExport" in {
        val request = buildGet(routes.NewOrExistingController.onPageLoad.url, aSessionId)
        val eventualResult = controller(journey, false).onPageLoad(request)

        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.GoodsDestinationController.onPageLoad().url)
      }
    }

    "onSubmit" should {
      s"redirect to /goods-destination after successful form submit with New for $importOrExport" in {
        val request = buildPost(routes.NewOrExistingController.onSubmit().url, aSessionId)
          .withFormUrlEncodedBody("value" -> "New")

        val eventualResult = controller(journey).onSubmit(request)
        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.GoodsDestinationController.onPageLoad().url)
      }

      s"redirect to /retrieve-declaration after successful form submit with 'Add goods to an existing declaration' for $importOrExport" in {
        val request = buildPost(routes.NewOrExistingController.onSubmit().url, aSessionId)
          .withFormUrlEncodedBody("value" -> "Amend")

        val eventualResult = controller(journey).onSubmit(request)
        status(eventualResult) mustBe 303
        redirectLocation(eventualResult) mustBe Some(routes.RetrieveDeclarationController.onPageLoad().url)
      }
    }

    s"return 400 with any form errors for $importOrExport" in {
      val request = buildPost(routes.NewOrExistingController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(journey).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
    }
  }
}
