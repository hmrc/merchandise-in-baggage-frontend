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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.views.html.JourneyDetailsPage

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class JourneyDetailsControllerSpec extends DeclarationJourneyControllerSpec {

  private val view: JourneyDetailsPage                                   = app.injector.instanceOf[JourneyDetailsPage]
  private val mockNavigator: Navigator                                   = mock(classOf[Navigator])
  private val controller: DeclarationJourney => JourneyDetailsController =
    declarationJourney =>
      new JourneyDetailsController(
        controllerComponents,
        stubProvider(declarationJourney),
        stubRepo(declarationJourney),
        view,
        mockNavigator
      )

  declarationTypes.foreach { importOrExport =>
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport, isAssistedDigital = false)
        .copy(maybeIsACustomsAgent = Some(YesNo.No))

    "onPageLoad" should {
      s"return 200 with correct content for $importOrExport" in {
        val request        = buildGet(JourneyDetailsController.onPageLoad.url, aSessionId, journey)
        val eventualResult = controller(journey).onPageLoad(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe OK
        result must include(messageApi("journeyDetails.title"))
        result must include(messageApi("journeyDetails.heading"))
        result must include(messageApi(s"journeyDetails.port.$importOrExport.label"))
        result must include(messageApi("journeyDetails.port.hint"))
        result must include(messageApi(s"journeyDetails.dateOfTravel.$importOrExport.label"))
      }
    }

    "onSubmit" should {
      s"redirect to next page after successful form submit for $importOrExport" in {
        val today   = LocalDate.now()
        val request =
          buildPost(
            JourneyDetailsController.onSubmit.url,
            aSessionId,
            journey,
            formData = Seq(
              "port"               -> "ABZ",
              "dateOfTravel.day"   -> today.getDayOfMonth.toString,
              "dateOfTravel.month" -> today.getMonthValue.toString,
              "dateOfTravel.year"  -> today.getYear.toString
            )
          )

        when(mockNavigator.nextPage(any[JourneyDetailsRequest])(any[ExecutionContext]))
          .thenReturn(Future.successful(GoodsInVehicleController.onPageLoad))

        val result: Future[Result] = controller(journey).onSubmit(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/declare-commercial-goods/goods-in-vehicle")
      }

      s"return 400 with any form errors for $importOrExport" in {
        val request =
          buildPost(
            JourneyDetailsController.onSubmit.url,
            aSessionId,
            journey,
            formData = Seq("port111" -> "ABZ")
          )

        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe BAD_REQUEST
        result must include(messageApi("journeyDetails.title"))
        result must include(messageApi("journeyDetails.heading"))
        result must include(messageApi(s"journeyDetails.port.$importOrExport.label"))
        result must include(messageApi("journeyDetails.port.hint"))
        result must include(messageApi(s"journeyDetails.dateOfTravel.$importOrExport.label"))
      }
    }
  }
}
