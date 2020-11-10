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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.merchandiseinbaggagefrontend.connectors.PaymentConnector
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.CheckYourAnswersForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.PayApiRequestBuilder
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationGoods
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.CheckYourAnswersPage

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                           actionProvider: DeclarationJourneyActionProvider,
                                           calculationService: CalculationService,
                                           connector: PaymentConnector,
                                           page: CheckYourAnswersPage)
                                          (implicit ec: ExecutionContext, appConfig: AppConfig, errorHandler: ErrorHandler)
  extends DeclarationJourneyController with PayApiRequestBuilder {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.declarationIfRequiredAndComplete.fold(actionProvider.invalidRequestF){ declaration =>
      calculationService.paymentCalculation(declaration.declarationGoods).map { paymentCalculations =>
        if(declaration.declarationType == Import
          && paymentCalculations.totalGbpValue.value > declaration.goodsDestination.threshold.value) {
          Redirect(routes.GoodsOverThresholdController.onPageLoad())
        } else {
          val taxDue = paymentCalculations.totalTaxDue
          Ok(page(form, declaration, taxDue))
        }
      }
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.goodsEntries.declarationGoodsIfComplete
      .fold(actionProvider.invalidRequestF)(goods => declarationConfirmation(request, goods))
    }

  private def declarationConfirmation(request: DeclarationJourneyRequest[AnyContent], goods: DeclarationGoods)
                                     (implicit headerCarrier: HeaderCarrier): Future[Result] = {
    request.declarationJourney.declarationType match {
      case Export => Future.successful(Redirect(routes.DeclarationConfirmationController.onPageLoad()))
      case Import => makePayment(goods).map(res => Redirect(connector.extractUrl(res).nextUrl.value))
    }
  }

  private def makePayment(goods: DeclarationGoods)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] =
    for {
      payApiRequest <- buildRequest(goods, calculationService.paymentCalculation)
      response      <- connector.makePayment(payApiRequest)
    } yield response
}
