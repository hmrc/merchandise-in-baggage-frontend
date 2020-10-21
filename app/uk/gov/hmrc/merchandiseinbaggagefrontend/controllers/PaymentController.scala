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

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.merchandiseinbaggagefrontend.connectors.PaymentConnector
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.CheckYourAnswersForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.AmountInPence
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.PaymentPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentController @Inject()(actionProvider: DeclarationJourneyActionProvider,
                                  override val controllerComponents: MessagesControllerComponents,
                                  paymentPage: PaymentPage, connector: PaymentConnector, calculationService: CalculationService)
                                 (implicit val ec: ExecutionContext, appConfig: AppConfig, errorHandler: ErrorHandler)
  extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(paymentPage()))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    val entries = request.declarationJourney.goodsEntries
    val ifComplete = entries.declarationGoodsIfComplete

    ifComplete.fold(actionProvider.invalidRequestF)(goods => {
      val eventualResponse = for {
        taxDue <- calculationService.taxCalculation(goods)
        body = PayApiRequest(
          MibReference("MIBI1234567890"),
          taxDue.totalTaxDue,
          AmountInPence(123),
          AmountInPence(123)
        )
        response <- connector.makePayment(body)
      } yield response


      eventualResponse.map(res =>
        Redirect(connector.extractUrl(res).nextUrl.value)).recoverWith {
        case _: Throwable => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }
    }
    )
  }
}

