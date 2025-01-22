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
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationType, Paid, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.service.MibService
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.views.html.PreviousDeclarationDetailsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PreviousDeclarationDetailsControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  val mockNavigator: Navigator   = mock(classOf[Navigator])
  val mockMibService: MibService = mock(classOf[MibService])

  val view: PreviousDeclarationDetailsView = app.injector.instanceOf[PreviousDeclarationDetailsView]
  val mibConnector: MibConnector           = injector.instanceOf[MibConnector]

  val controller: PreviousDeclarationDetailsController =
    new PreviousDeclarationDetailsController(
      controllerComponents,
      actionBuilder,
      declarationJourneyRepository,
      mibConnector,
      mockNavigator,
      mockMibService,
      view
    )

  "creating a page" should {
    "return 200 if declaration exists" in {
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Import,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId
        )

      givenADeclarationJourneyIsPersisted(importJourney)

      when(mockMibService.thresholdAllowance(any[Declaration])(any[HeaderCarrier]))
        .thenReturn(Future.successful(aThresholdAllowance))

      val persistedDeclaration: Option[Declaration] = importJourney.declarationIfRequiredAndComplete.map {
        declaration =>
          declaration.copy(maybeTotalCalculationResult = Some(aTotalCalculationResult))
      }

      givenPersistedDeclarationIsFound(persistedDeclaration.get, aDeclarationId)

      val request        = buildGet(PreviousDeclarationDetailsController.onPageLoad.url, aSessionId, importJourney)
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe OK

      contentAsString(eventualResult) must include("cheese")
    }

    "return 303 if declaration does NOT exist" in {
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Import,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId
        )

      givenADeclarationJourneyIsPersisted(importJourney)

      givenPersistedDeclarationIsFound(
        importJourney.declarationIfRequiredAndComplete.get,
        aDeclarationId
      )

      val request =
        buildGet(
          PreviousDeclarationDetailsController.onPageLoad.url,
          SessionId(),
          importJourney,
          sessionData = Seq("declarationId" -> "987")
        )

      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe SEE_OTHER

      contentAsString(eventualResult) mustNot include("cheese")
    }

    "return 200 if import declaration with amendment exists " in {
      val importJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Import,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId
        )

      when(mockMibService.thresholdAllowance(any[Declaration])(any[HeaderCarrier]))
        .thenReturn(Future.successful(aThresholdAllowance))

      givenADeclarationJourneyIsPersisted(importJourney)

      val persistedDeclaration: Option[Declaration] = importJourney.declarationIfRequiredAndComplete.map {
        declaration =>
          declaration
            .copy(
              maybeTotalCalculationResult = Some(aTotalCalculationResult),
              paymentStatus = Some(Paid),
              amendments = Seq(aAmendmentPaid)
            )
      }

      givenPersistedDeclarationIsFound(persistedDeclaration.get, aDeclarationId)

      val request        = buildGet(PreviousDeclarationDetailsController.onPageLoad.url, aSessionId, importJourney)
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe OK

      contentAsString(eventualResult) must include("cheese")
      contentAsString(eventualResult) must include("more cheese")
      contentAsString(eventualResult) must include("Payment made")

    }

    "return 200 if export declaration with amendment exists " in {
      val exportJourney: DeclarationJourney = completedDeclarationJourney
        .copy(
          sessionId = aSessionId,
          declarationType = DeclarationType.Export,
          createdAt = journeyDate.atStartOfDay,
          declarationId = aDeclarationId
        )

      when(mockMibService.thresholdAllowance(any[Declaration])(any[HeaderCarrier]))
        .thenReturn(Future.successful(aThresholdAllowance))

      givenADeclarationJourneyIsPersisted(exportJourney)

      val persistedDeclaration: Option[Declaration] = exportJourney.declarationIfRequiredAndComplete.map {
        declaration =>
          declaration
            .copy(
              maybeTotalCalculationResult = Some(aTotalCalculationResult),
              paymentStatus = Some(Paid),
              amendments = Seq(aAmendmentPaid)
            )
      }

      givenPersistedDeclarationIsFound(persistedDeclaration.get, aDeclarationId)

      val request        = buildGet(PreviousDeclarationDetailsController.onPageLoad.url, aSessionId, exportJourney)
      val eventualResult = controller.onPageLoad()(request)
      status(eventualResult) mustBe OK

      contentAsString(eventualResult) must include("cheese")
      contentAsString(eventualResult) must include("more cheese")
      contentAsString(eventualResult) mustNot include("Payment made")
    }
  }

  "on submit update and redirect" in {
    val postRequest = buildPost(PreviousDeclarationDetailsController.onPageLoad.url, aSessionId)

    val result = controller.onSubmit()(postRequest)
    status(result) mustBe SEE_OTHER
  }
}
