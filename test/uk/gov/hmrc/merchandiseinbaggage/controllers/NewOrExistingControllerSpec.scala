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

import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.views.html.NewOrExistingView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class NewOrExistingControllerSpec extends DeclarationJourneyControllerSpec {

  private val view: NewOrExistingView                                             = injector.instanceOf[NewOrExistingView]
  private val mockNavigator: Navigator                                            = mock[Navigator]
  def controller(declarationJourney: DeclarationJourney): NewOrExistingController =
    new NewOrExistingController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      view,
      mockNavigator
    )

  declarationTypes.foreach { importOrExport: DeclarationType =>
    val journey: DeclarationJourney = DeclarationJourney(aSessionId, importOrExport)
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request        = buildGet(NewOrExistingController.onPageLoad.url, aSessionId)
        val eventualResult = controller(journey).onPageLoad(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe OK
        result must include(messageApi(s"newOrExisting.title"))
        result must include(messageApi(s"newOrExisting.heading"))
        result must include(messageApi(s"service.name.${importOrExport.entryName}.a.href"))
      }
    }

    "onSubmit" should {
      s"redirect to /goods-destination after successful form submit with New for $importOrExport" in {
        val request = buildPost(NewOrExistingController.onSubmit.url, aSessionId)
          .withFormUrlEncodedBody("value" -> "New")

        when(mockNavigator.nextPage(any[NewOrExistingRequest])(any[ExecutionContext]))
          .thenReturn(Future.successful(GoodsDestinationController.onPageLoad))

        val result: Future[Result] = controller(journey).onSubmit(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/declare-commercial-goods/goods-destination")
        session(result).get("journeyType") mustBe Some("new")
      }

      s"redirect to /retrieve-declaration after successful form submit with 'Add goods to an existing declaration' for $importOrExport" in {
        val request = buildPost(NewOrExistingController.onSubmit.url, aSessionId)
          .withFormUrlEncodedBody("value" -> "Amend")

        when(mockNavigator.nextPage(any[NewOrExistingRequest])(any[ExecutionContext]))
          .thenReturn(Future.successful(RetrieveDeclarationController.onPageLoad))

        val result: Future[Result] = controller(journey).onSubmit(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/declare-commercial-goods/retrieve-declaration")
        session(result).get("journeyType") mustBe Some("amend")
      }
    }

    s"return 400 with any form errors for $importOrExport" in {
      val request        = buildPost(NewOrExistingController.onSubmit.url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      val eventualResult = controller(journey).onSubmit(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe BAD_REQUEST
      result must include(messageApi("error.summary.title"))
    }
  }
}
