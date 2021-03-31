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

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.views.html.JourneyDetailsPage
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import java.time.LocalDate

import org.scalamock.scalatest.MockFactory

import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.merchandiseinbaggage.navigation._

class JourneyDetailsControllerSpec extends DeclarationJourneyControllerSpec with MockFactory {

  private val view = app.injector.instanceOf[JourneyDetailsPage]
  private val mockNavigator = mock[Navigator]
  private val controller: DeclarationJourney => JourneyDetailsController =
    declarationJourney =>
      new JourneyDetailsController(
        controllerComponents,
        stubProvider(declarationJourney),
        stubRepo(declarationJourney),
        view,
        mockNavigator)

  declarationTypes.foreach { importOrExport =>
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport).copy(maybeIsACustomsAgent = Some(YesNo.No))

    "onPageLoad" should {
      s"return 200 with correct content for $importOrExport" in {
        val request = buildGet(JourneyDetailsController.onPageLoad().url, aSessionId)
        val eventualResult = controller(journey).onPageLoad(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        result must include(messageApi("journeyDetails.title"))
        result must include(messageApi("journeyDetails.heading"))
        result must include(messageApi(s"journeyDetails.port.$importOrExport.label"))
        result must include(messageApi("journeyDetails.port.hint"))
        result must include(messageApi(s"journeyDetails.dateOfTravel.$importOrExport.label"))
      }
    }

    "onSubmit" should {
      s"redirect to next page after successful form submit for $importOrExport" in {
        val today = LocalDate.now()
        val request = buildPost(JourneyDetailsController.onSubmit().url, aSessionId)
          .withFormUrlEncodedBody(
            "port"               -> "ABZ",
            "dateOfTravel.day"   -> today.getDayOfMonth.toString,
            "dateOfTravel.month" -> today.getMonthValue.toString,
            "dateOfTravel.year"  -> today.getYear.toString
          )

        (mockNavigator
          .nextPage(_: RequestByPass))
          .expects(RequestByPass(JourneyDetailsController.onPageLoad().url))
          .returning(GoodsInVehicleController.onPageLoad())

        controller(journey).onSubmit(request).futureValue
      }

      s"return 400 with any form errors for $importOrExport" in {
        val request = buildPost(JourneyDetailsController.onSubmit().url, aSessionId)
          .withFormUrlEncodedBody("port111" -> "ABZ")

        val eventualResult = controller(givenADeclarationJourneyIsPersisted(journey)).onSubmit(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 400
        result must include(messageApi("journeyDetails.title"))
        result must include(messageApi("journeyDetails.heading"))
        result must include(messageApi(s"journeyDetails.port.$importOrExport.label"))
        result must include(messageApi("journeyDetails.port.hint"))
        result must include(messageApi(s"journeyDetails.dateOfTravel.$importOrExport.label"))
      }
    }
  }
}
