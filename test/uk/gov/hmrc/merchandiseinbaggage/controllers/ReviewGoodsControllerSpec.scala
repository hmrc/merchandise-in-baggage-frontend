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

import cats.data.OptionT
import org.scalamock.scalatest.MockFactory
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationId, GoodsDestination, JourneyType}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, CalculationResults, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries}
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.service.MibService
import uk.gov.hmrc.merchandiseinbaggage.views.html.ReviewGoodsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import com.softwaremill.quicklens._

class ReviewGoodsControllerSpec extends DeclarationJourneyControllerSpec with MockFactory with PropertyBaseTables {

  private val view = app.injector.instanceOf[ReviewGoodsView]
  private val mockNavigator = mock[Navigator]
  private val mockMibService = mock[MibService]

  def controller(declarationJourney: DeclarationJourney) =
    new ReviewGoodsController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      view,
      mockMibService,
      mockNavigator)

  forAll(declarationTypesTable) { importOrExport =>
    val entries = completedGoodsEntries(importOrExport)
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport, goodsEntries = entries)

    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        val request = buildGet(ReviewGoodsController.onPageLoad().url, aSessionId)
        val allowance =
          if (importOrExport == Export) aThresholdAllowance.modify(_.currentGoods).setTo(entries.declarationGoodsIfComplete.get)
          else aThresholdAllowance
        (mockMibService
          .thresholdAllowance(_: Option[GoodsDestination], _: GoodsEntries, _: JourneyType, _: DeclarationId)(_: HeaderCarrier))
          .expects(*, *, *, *, *)
          .returning(OptionT.pure[Future](allowance))
          .once()

        val eventualResult = controller(journey).onPageLoad()(request)
        val result = contentAsString(eventualResult)

        status(eventualResult) mustBe 200
        if (importOrExport == Import) {
          result must include(messageApi("reviewGoods.list.vatRate"))
          result must include(messageApi("reviewGoods.list.producedInEu"))
        }
        if (importOrExport == Export) { result must include(messageApi("reviewGoods.list.destination")) }
      }
    }

    "onSubmit" should {
      s"redirect to next page after successful form submit with Yes for $importOrExport by delegating to Navigator" in {
        val request = buildPost(ReviewGoodsController.onSubmit().url, aSessionId)
          .withFormUrlEncodedBody("value" -> "Yes")

        (mockMibService
          .thresholdAllowance(_: Option[GoodsDestination], _: GoodsEntries, _: JourneyType, _: DeclarationId)(_: HeaderCarrier))
          .expects(*, *, *, *, *)
          .returning(OptionT.pure[Future](aThresholdAllowance))
          .once()

        (mockNavigator
          .nextPage(_: ReviewGoodsRequest)(_: ExecutionContext))
          .expects(*, *)
          .returning(Future.successful(GoodsTypeController.onPageLoad(2)))
          .once()

        controller(journey).onSubmit(request).futureValue
      }
    }

    s"return 400 with any form errors for $importOrExport" in {
      val request = buildPost(ReviewGoodsController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      (mockMibService
        .thresholdAllowance(_: Option[GoodsDestination], _: GoodsEntries, _: JourneyType, _: DeclarationId)(_: HeaderCarrier))
        .expects(*, *, *, *, *)
        .returning(OptionT.pure[Future](aThresholdAllowance))
        .once()

      val eventualResult = controller(journey).onSubmit(request)
      val result = contentAsString(eventualResult)

      status(eventualResult) mustBe 400
      result must include(messageApi("error.summary.title"))
      result must include(messageApi("reviewGoods.New.title"))
      result must include(messageApi("reviewGoods.New.heading"))
    }
  }

  forAll(declarationTypesTable) { importOrExport =>
    s"redirect to next page after successful form submit with No for $importOrExport" in {
      val id = aSessionId
      val journey: DeclarationJourney =
        DeclarationJourney(id, importOrExport, goodsEntries = completedGoodsEntries(importOrExport))
          .copy(journeyType = Amend)

      val controller =
        new ReviewGoodsController(
          controllerComponents,
          stubProvider(journey),
          stubRepo(journey),
          view,
          mockMibService,
          injector.instanceOf[Navigator])

      (mockMibService
        .thresholdAllowance(_: Option[GoodsDestination], _: GoodsEntries, _: JourneyType, _: DeclarationId)(_: HeaderCarrier))
        .expects(*, *, *, *, *)
        .returning(OptionT.pure[Future](aThresholdAllowance))
        .once()

      (mockMibService
        .amendPlusOriginalCalculations(_: DeclarationJourney)(_: HeaderCarrier))
        .expects(*, *)
        .returning(OptionT.pure[Future](CalculationResponse(CalculationResults(Seq.empty), WithinThreshold)))

      val request = buildPost(ReviewGoodsController.onSubmit().url, id)
        .withFormUrlEncodedBody("value" -> "No")

      val eventualResult = controller.onSubmit()(request)
      val expectedRedirect =
        if (importOrExport == Export) Some(CheckYourAnswersController.onPageLoad().url)
        else Some(PaymentCalculationController.onPageLoad().url)
      status(eventualResult) mustBe 303
      redirectLocation(eventualResult) mustBe expectedRedirect
    }
  }
}
