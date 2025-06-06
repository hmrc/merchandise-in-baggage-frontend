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
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.views.html.ExciseAndRestrictedGoodsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ExciseAndRestrictedGoodsControllerSpec extends DeclarationJourneyControllerSpec with PropertyBaseTables {

  val view: ExciseAndRestrictedGoodsView                                                     = app.injector.instanceOf[ExciseAndRestrictedGoodsView]
  val mockNavigator: Navigator                                                               = mock(classOf[Navigator])
  def controller(declarationJourney: DeclarationJourney): ExciseAndRestrictedGoodsController =
    new ExciseAndRestrictedGoodsController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      view,
      mockNavigator
    )

  forAll(declarationTypesTable) { (importOrExport: DeclarationType) =>
    val journey: DeclarationJourney = DeclarationJourney(aSessionId, importOrExport, isAssistedDigital = false)
    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {

        val request        = buildGet(ExciseAndRestrictedGoodsController.onPageLoad.url, aSessionId, journey)
        val eventualResult = controller(journey).onPageLoad(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe OK
        result must include(messages(s"exciseAndRestrictedGoods.$importOrExport.title"))
        result must include(messages(s"exciseAndRestrictedGoods.$importOrExport.heading"))
        result must include(messages("exciseAndRestrictedGoods.details"))
      }
    }

    "onSubmit" should {
      s"redirect by delegating to the Navigator for $importOrExport" in {
        val request =
          buildPost(
            ExciseAndRestrictedGoodsController.onSubmit.url,
            aSessionId,
            journey,
            formData = Seq("value" -> "No")
          )

        when(mockNavigator.nextPage(any[ExciseAndRestrictedGoodsRequest])(any[ExecutionContext]))
          .thenReturn(Future.successful(ValueWeightOfGoodsController.onPageLoad))

        val result: Future[Result] = controller(journey).onSubmit(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/declare-commercial-goods/value-weight-of-goods")
      }
    }

    s"return 400 with any form errors for $importOrExport" in {
      val request =
        buildPost(
          ExciseAndRestrictedGoodsController.onSubmit.url,
          aSessionId,
          journey,
          formData = Seq("value" -> "in valid")
        )

      val eventualResult = controller(journey).onSubmit(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe BAD_REQUEST
      result must include(messages("error.summary.title"))
      result must include(messages(s"exciseAndRestrictedGoods.$importOrExport.title"))
      result must include(messages(s"exciseAndRestrictedGoods.$importOrExport.heading"))
    }
  }
}
