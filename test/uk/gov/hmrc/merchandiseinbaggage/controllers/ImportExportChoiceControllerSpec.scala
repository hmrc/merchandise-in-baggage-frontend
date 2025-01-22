/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.mvc.Result
import play.api.test.Helpers.*
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes.*
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.model.core.ImportExportChoices.{AddToExisting, MakeExport}
import uk.gov.hmrc.merchandiseinbaggage.navigation.ImportExportChoiceRequest
import uk.gov.hmrc.merchandiseinbaggage.views.html.{ImportExportChoice, PageNotFoundView}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.MockStrideAuth.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ImportExportChoiceControllerSpec extends DeclarationJourneyControllerSpec {

  val importExportView: ImportExportChoice     = injector.instanceOf[ImportExportChoice]
  private val pageNotFoundView                 = app.injector.instanceOf[PageNotFoundView]
  val mockNavigator: Navigator                 = mock(classOf[Navigator])
  val journey: DeclarationJourney              = startedImportJourney.copy(isAssistedDigital = true)
  val controller: ImportExportChoiceController =
    new ImportExportChoiceController(
      controllerComponents,
      importExportView,
      pageNotFoundView,
      actionBuilder,
      stubRepo(journey),
      mockNavigator
    )

  "onPageLoad" should {
    "return 200 with radio button" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request = buildGet(ImportExportChoiceController.onPageLoad.url, aSessionId, journey)

      val eventualResult = controller.onPageLoad(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe OK
      result must include(messages("importExportChoice.header"))
      result must include(messages("importExportChoice.title"))
      result must include(messages("importExportChoice.MakeImport"))
      result must include(messages("importExportChoice.MakeExport"))
      result must include(messages("importExportChoice.AddToExisting"))
    }

    "return 403 page not found" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val journey: DeclarationJourney = startedImportJourney.copy(isAssistedDigital = false)
      val request                     = buildGet(ImportExportChoiceController.onPageLoad.url, aSessionId, journey)

      val eventualResult = controller.onPageLoad(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe FORBIDDEN
      result must include(messages("pageNotFound.title"))
      result must include(messages("pageNotFound.li1"))
      result must include(messages("pageNotFound.li2"))
      result must include(messages("pageNotFound.li3"))
      result must include(messages("pageNotFound.Import.restart"))
      result must include(messages("pageNotFound.Export.restart"))
    }
  }

  "onSubmit" should {
    "redirect with navigator adding 'new' to header" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request =
        buildPost(
          ImportExportChoiceController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("value" -> MakeExport.toString)
        )

      when(mockNavigator.nextPage(any[ImportExportChoiceRequest])(any[ExecutionContext]))
        .thenReturn(Future.successful(GoodsDestinationController.onPageLoad))

      val result: Future[Result] = controller.onSubmit(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/declare-commercial-goods/goods-destination")
      session(result).get("journeyType") mustBe Some("new")
    }

    "redirect with navigator adding 'amend' to header" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request =
        buildPost(
          routes.ImportExportChoiceController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("value" -> AddToExisting.toString)
        )

      when(mockNavigator.nextPage(any[ImportExportChoiceRequest])(any[ExecutionContext]))
        .thenReturn(Future.successful(GoodsDestinationController.onPageLoad))

      val result: Future[Result] = controller.onSubmit(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/declare-commercial-goods/goods-destination")
      session(result).get("journeyType") mustBe Some("amend")
    }

    "return 400 with required form error" in {
      givenTheUserIsAuthenticatedAndAuthorised()

      val request =
        buildPost(
          ImportExportChoiceController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("value" -> "")
        )

      givenTheUserIsAuthenticatedAndAuthorised()

      val eventualResult = controller.onSubmit(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe BAD_REQUEST
      result must include(messages("error.summary.title"))
      result must include(messages("importExportChoice.header"))
      result must include(messages("importExportChoice.error.required"))
    }
  }
}
