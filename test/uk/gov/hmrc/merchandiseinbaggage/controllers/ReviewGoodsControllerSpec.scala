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

import cats.data.OptionT
import com.softwaremill.quicklens._
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResponse, CalculationResults, OverThreshold, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationId, GoodsDestination, JourneyType}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries, ThresholdAllowance}
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.service.MibService
import uk.gov.hmrc.merchandiseinbaggage.views.html.ReviewGoodsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ReviewGoodsControllerSpec extends DeclarationJourneyControllerSpec with PropertyBaseTables {

  private val view: ReviewGoodsView      = app.injector.instanceOf[ReviewGoodsView]
  private val mockNavigator: Navigator   = mock[Navigator]
  private val mockMibService: MibService = mock[MibService]

  def controller(declarationJourney: DeclarationJourney): ReviewGoodsController =
    new ReviewGoodsController(
      controllerComponents,
      stubProvider(declarationJourney),
      stubRepo(declarationJourney),
      view,
      mockMibService,
      mockNavigator
    )

  forAll(declarationTypesTable) { importOrExport =>
    val entries                     = completedGoodsEntries(importOrExport)
    val journey: DeclarationJourney =
      DeclarationJourney(aSessionId, importOrExport, goodsEntries = entries)

    "onPageLoad" should {
      s"return 200 with radio buttons for $importOrExport" in {
        val request   = buildGet(ReviewGoodsController.onPageLoad.url, aSessionId)
        val allowance =
          if (importOrExport == Export) {
            aThresholdAllowance.modify(_.currentGoods).setTo(entries.declarationGoodsIfComplete.get)
          } else {
            aThresholdAllowance
          }

        when(
          mockMibService.thresholdAllowance(
            any[Option[GoodsDestination]],
            any[GoodsEntries],
            any[JourneyType],
            any[DeclarationId]
          )(any[HeaderCarrier])
        )
          .thenReturn(OptionT.pure[Future](allowance))

        val eventualResult = controller(journey).onPageLoad()(request)
        val result         = contentAsString(eventualResult)

        status(eventualResult) mustBe OK
        if (importOrExport == Import) {
          result must include(messageApi("reviewGoods.list.vatRate"))
          result must include(messageApi("reviewGoods.list.producedInEu"))
        } else {
          result must include(messageApi("reviewGoods.list.destination"))
        }
      }

      s"redirect to CannotAccessPageController when no ThresholdAllowance is received from mibService for $importOrExport" in {
        val request = buildGet(ReviewGoodsController.onPageLoad.url, aSessionId)

        when(
          mockMibService.thresholdAllowance(
            any[Option[GoodsDestination]],
            any[GoodsEntries],
            any[JourneyType],
            any[DeclarationId]
          )(any[HeaderCarrier])
        )
          .thenReturn(OptionT.none)

        val result = controller(journey).onPageLoad()(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(CannotAccessPageController.onPageLoad.url)

      }
    }

    "onSubmit" should {
      s"redirect to next page after successful form submit with Yes for $importOrExport by delegating to Navigator" in {
        val request = buildPost(ReviewGoodsController.onSubmit.url, aSessionId)
          .withFormUrlEncodedBody("value" -> "Yes")

        when(
          mockMibService.thresholdAllowance(
            any[Option[GoodsDestination]],
            any[GoodsEntries],
            any[JourneyType],
            any[DeclarationId]
          )(any[HeaderCarrier])
        )
          .thenReturn(OptionT.pure[Future](aThresholdAllowance))

        when(mockNavigator.nextPage(any[ReviewGoodsRequest])(any[ExecutionContext]))
          .thenReturn(Future.successful(GoodsTypeController.onPageLoad(2)))

        val result: Future[Result] = controller(journey).onSubmit(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/declare-commercial-goods/goods-type/2")
      }
    }

    s"return 400 with any form errors for $importOrExport" in {
      val request = buildPost(ReviewGoodsController.onSubmit.url, aSessionId)
        .withFormUrlEncodedBody("value" -> "in valid")

      when(
        mockMibService.thresholdAllowance(
          any[Option[GoodsDestination]],
          any[GoodsEntries],
          any[JourneyType],
          any[DeclarationId]
        )(any[HeaderCarrier])
      )
        .thenReturn(OptionT.pure[Future](aThresholdAllowance))

      val eventualResult = controller(journey).onSubmit(request)
      val result         = contentAsString(eventualResult)

      status(eventualResult) mustBe BAD_REQUEST
      result must include(messageApi("error.summary.title"))
      result must include(messageApi("reviewGoods.New.title"))
      result must include(messageApi("reviewGoods.New.heading"))
    }

    s"redirect to CannotAccessPage when no ThresholdAllowance is received from mibService for $importOrExport" in {
      val request = buildPost(ReviewGoodsController.onSubmit.url, aSessionId)

      when(
        mockMibService.thresholdAllowance(
          any[Option[GoodsDestination]],
          any[GoodsEntries],
          any[JourneyType],
          any[DeclarationId]
        )(any[HeaderCarrier])
      )
        .thenReturn(OptionT.none)

      val result = controller(journey).onSubmit()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(CannotAccessPageController.onPageLoad.url)

    }

    s"redirect to next page after successful form submit with No for $importOrExport" in {
      val journey: DeclarationJourney =
        DeclarationJourney(aSessionId, importOrExport, goodsEntries = completedGoodsEntries(importOrExport))
          .copy(journeyType = Amend)

      when(
        mockMibService.thresholdAllowance(
          any[Option[GoodsDestination]],
          any[GoodsEntries],
          any[JourneyType],
          any[DeclarationId]
        )(any[HeaderCarrier])
      )
        .thenReturn(OptionT.pure[Future](aThresholdAllowance))

      when(mockMibService.amendPlusOriginalCalculations(any[DeclarationJourney])(any[HeaderCarrier]))
        .thenReturn(OptionT.pure[Future](CalculationResponse(CalculationResults(Seq.empty), WithinThreshold)))

      val request                = buildPost(ReviewGoodsController.onSubmit.url, aSessionId)
        .withFormUrlEncodedBody("value" -> "No")

      val expectedRedirect       =
        if (importOrExport == Export) {
          when(mockNavigator.nextPage(any[ReviewGoodsRequest])(any[ExecutionContext]))
            .thenReturn(Future.successful(CheckYourAnswersController.onPageLoad))
          Some(CheckYourAnswersController.onPageLoad.url)
        } else {
          when(mockNavigator.nextPage(any[ReviewGoodsRequest])(any[ExecutionContext]))
            .thenReturn(Future.successful(PaymentCalculationController.onPageLoad))
          Some(PaymentCalculationController.onPageLoad.url)
        }
      val result: Future[Result] = controller(journey).onSubmit()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe expectedRedirect
    }

    s"redirect to GoodsOverThreshold page after form is submitted when allowance has been exceeded for $importOrExport" in {

      val overThresholdCalculationResponse: CalculationResponse =
        aCalculationResponse.copy(thresholdCheck = OverThreshold)
      val overThresholdAllowance: ThresholdAllowance            =
        aThresholdAllowance.copy(calculationResponse = overThresholdCalculationResponse)

      when(
        mockMibService.thresholdAllowance(
          any[Option[GoodsDestination]],
          any[GoodsEntries],
          any[JourneyType],
          any[DeclarationId]
        )(any[HeaderCarrier])
      )
        .thenReturn(OptionT.pure[Future](overThresholdAllowance))

      when(mockNavigator.nextPage(any[ReviewGoodsRequest])(any[ExecutionContext]))
        .thenReturn(Future.successful(GoodsOverThresholdController.onPageLoad))

      val request = buildPost(ReviewGoodsController.onSubmit.url, aSessionId)

      val result: Future[Result] = controller(journey).onSubmit()(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(GoodsOverThresholdController.onPageLoad.url)

    }
  }
}
