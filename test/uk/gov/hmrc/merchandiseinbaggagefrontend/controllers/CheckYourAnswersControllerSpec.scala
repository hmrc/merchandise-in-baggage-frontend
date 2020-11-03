/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, SessionKeys}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.ErrorHandler
import uk.gov.hmrc.merchandiseinbaggagefrontend.connectors.{CurrencyConversionConnector, PaymentConnector}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.PayApiRequest
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{DeclarationGoods, DeclarationType, PaymentCalculations, SessionId}
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.CheckYourAnswersPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val httpClient = injector.instanceOf[HttpClient]
  private val page = injector.instanceOf[CheckYourAnswersPage]
  private lazy val conversionConnector = injector.instanceOf[CurrencyConversionConnector]
  private implicit lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]
  private val stubbedApiResponse = s"""{"journeyId":"5f3b","nextUrl":"http://host"}"""

  val testConnector = new PaymentConnector(httpClient, ""){
    override def makePayment(requestBody: PayApiRequest)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
      Future.successful(HttpResponse(201, stubbedApiResponse))
  }

  val stubbedCalculation = new CalculationService(conversionConnector) {
    override def paymentCalculation(declarationGoods: DeclarationGoods)(implicit hc: HeaderCarrier): Future[PaymentCalculations] =
      Future.successful(aPaymentCalculations)
  }

  val controller = new CheckYourAnswersController(controllerComponents, actionBuilder, stubbedCalculation, testConnector, page)

  "on submit will calculate tax and send payment request to pay api" in {
    val sessionId = SessionId()
    givenADeclarationJourneyIsPersisted(completedDeclarationJourney.copy(sessionId = sessionId))
    val request = buildPost(routes.CheckYourAnswersController.onSubmit().url).withSession(SessionKeys.sessionId -> sessionId.value)

    val eventualResult = controller.onSubmit()(request)

    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some("http://host")
  }

  "on submit will redirect to declaration-confirmation if exporting" in {
    val sessionId = SessionId()
    val declarationJourney = completedDeclarationJourney
      .copy(sessionId = sessionId)
      .copy(declarationType = DeclarationType.Export)
    val request = buildPost(routes.CheckYourAnswersController.onSubmit().url).withSession(SessionKeys.sessionId -> sessionId.value)

    givenADeclarationJourneyIsPersisted(declarationJourney)

    val eventualResult = controller.onSubmit()(request)

    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some("declaration-confirmation")
  }
}
