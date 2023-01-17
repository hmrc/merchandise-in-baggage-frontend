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

import org.scalamock.scalatest.MockFactory
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.core.ImportExportChoices.{AddToExisting, MakeExport}
import uk.gov.hmrc.merchandiseinbaggage.navigation.ImportExportChoiceRequest
import uk.gov.hmrc.merchandiseinbaggage.views.html.ImportExportChoice
import uk.gov.hmrc.merchandiseinbaggage.wiremock.MockStrideAuth._
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ImportExportChoiceControllerSpec extends DeclarationJourneyControllerSpec with MockFactory with WireMockSupport {

  val view = injector.instanceOf[ImportExportChoice]
  val mockNavigator = mock[Navigator]
  val controller =
    new ImportExportChoiceController(controllerComponents, view, actionBuilder, stubRepo(startedImportJourney), mockNavigator)

  "onPageLoad" should {
    "return 200 with radio button" in {
      val request = buildGet(ImportExportChoiceController.onPageLoad.url, aSessionId)
      givenTheUserIsAuthenticatedAndAuthorised()

      val eventualResult = controller.onPageLoad(request)
      val result = contentAsString(eventualResult)
      status(eventualResult) mustBe 200
      result must include(messageApi("importExportChoice.header"))
      result must include(messageApi("importExportChoice.title"))
      result must include(messageApi("importExportChoice.MakeImport"))
      result must include(messageApi("importExportChoice.MakeExport"))
      result must include(messageApi("importExportChoice.AddToExisting"))
    }
  }

  "onSubmit" should {
    "redirect with navigator adding 'new' to header" in {
      val request = buildGet(ImportExportChoiceController.onSubmit.url, aSessionId)
        .withFormUrlEncodedBody("value" -> MakeExport.toString)

      (mockNavigator
        .nextPage(_: ImportExportChoiceRequest)(_: ExecutionContext))
        .expects(*, *)
        .returning(Future successful GoodsDestinationController.onPageLoad)
        .once()

      val eventualResult = controller.onSubmit(request)
      session(eventualResult).get("journeyType") mustBe Some("new")
    }

    "redirect with navigator adding 'amend' to header" in {
      val request = buildGet(routes.ImportExportChoiceController.onSubmit.url, aSessionId)
        .withFormUrlEncodedBody("value" -> AddToExisting.toString)

      (mockNavigator
        .nextPage(_: ImportExportChoiceRequest)(_: ExecutionContext))
        .expects(*, *)
        .returning(Future successful GoodsDestinationController.onPageLoad)
        .once()

      val eventualResult = controller.onSubmit(request)
      session(eventualResult).get("journeyType") mustBe Some("amend")
    }

    "return 400 with required form error" in {
      val request = buildGet(ImportExportChoiceController.onSubmit.url, aSessionId)
        .withFormUrlEncodedBody("value" -> "")

      givenTheUserIsAuthenticatedAndAuthorised()

      val eventualResult = controller.onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messageApi("importExportChoice.header"))
      result must include(messageApi("importExportChoice.error.required"))
    }
  }
}
