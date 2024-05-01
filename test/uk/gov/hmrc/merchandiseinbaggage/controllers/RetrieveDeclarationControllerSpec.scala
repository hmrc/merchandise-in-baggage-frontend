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
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenFindByDeclarationReturnStatus
import uk.gov.hmrc.merchandiseinbaggage.views.html.RetrieveDeclarationView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class RetrieveDeclarationControllerSpec extends DeclarationJourneyControllerSpec with PropertyBaseTables {

  val view: RetrieveDeclarationView = injector.instanceOf[RetrieveDeclarationView]
  val connector: MibConnector       = injector.instanceOf[MibConnector]
  val mockNavigator: Navigator      = mock[Navigator]

  def controller(declarationJourney: DeclarationJourney): RetrieveDeclarationController =
    new RetrieveDeclarationController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      connector,
      mockNavigator,
      view
    )

  val journey: DeclarationJourney = DeclarationJourney(aSessionId, Import, isAssistedDigital = false)

  //TODO create UI test for content
  forAll(declarationTypesTable) { importOrExport: DeclarationType =>
    val journey: DeclarationJourney = DeclarationJourney(aSessionId, importOrExport, isAssistedDigital = false)
    "onPageLoad" should {
      s"return 200 with expected content for $importOrExport" in {

        val request        = buildGet(RetrieveDeclarationController.onPageLoad.url, aSessionId, journey)
        val eventualResult = controller(journey).onPageLoad(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe OK
        result must include(messageApi(s"retrieveDeclaration.title"))
        result must include(messageApi(s"retrieveDeclaration.heading"))
        result must include(messageApi(s"retrieveDeclaration.p"))

        result must include(messageApi(s"retrieveDeclaration.mibReference.label"))
        result must include(messageApi(s"retrieveDeclaration.mibReference.hint"))

        result must include(messageApi(s"retrieveDeclaration.eori.label"))
        result must include(messageApi(s"retrieveDeclaration.eori.hint"))
      }
    }
  }

  "onSubmit" should {
    "redirect by delegating to Navigator" in {
      givenFindByDeclarationReturnStatus(mibReference, eori, NOT_FOUND)
      val request =
        buildPost(
          RetrieveDeclarationController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("mibReference" -> mibReference.value, "eori" -> eori.value)
        )

      when(mockNavigator.nextPage(any[NavigationRequest])(any[ExecutionContext]))
        .thenReturn(Future.successful(DeclarationNotFoundController.onPageLoad))

      val result: Future[Result] = controller(journey).onSubmit(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some("/declare-commercial-goods/declaration-not-found")
    }

    "redirect to /internal-server-error after successful form submit but some unexpected error is thrown from the BE" in {
      givenFindByDeclarationReturnStatus(mibReference, eori, INTERNAL_SERVER_ERROR)
      val request =
        buildPost(
          RetrieveDeclarationController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("mibReference" -> mibReference.value, "eori" -> eori.value)
        )

      val result: Future[Result] = controller(journey).onSubmit(request)

      status(result) mustBe INTERNAL_SERVER_ERROR

    }

    "return 400 for invalid form data" in {
      val request =
        buildPost(
          RetrieveDeclarationController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("mibReference" -> "XAMB0000010", "eori" -> "GB12345")
        )

      val eventualResult = controller(journey).onSubmit(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe BAD_REQUEST
      result must include(messageApi(s"retrieveDeclaration.mibReference.error.invalid"))
      result must include(messageApi(s"retrieveDeclaration.eori.error.invalid"))
    }
  }
}
