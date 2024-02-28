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

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.{goodsDeclarationIncompleteMessage, goodsDestinationUnansweredMessage}
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResults, OverThreshold, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.service.MibService
import uk.gov.hmrc.merchandiseinbaggage.views.html.PaymentCalculationView

import scala.concurrent.ExecutionContext

@Singleton
class PaymentCalculationController @Inject() (
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  mibService: MibService,
  mibConnector: MibConnector,
  view: PaymentCalculationView
)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends DeclarationJourneyController {

  private val backButtonUrl: Call =
    ReviewGoodsController.onPageLoad

  private def checkYourAnswersIfComplete(default: Call)(implicit request: DeclarationJourneyRequest[_]): Call =
    if (request.declarationJourney.declarationRequiredAndComplete || request.declarationJourney.journeyType == Amend) {
      CheckYourAnswersController.onPageLoad
    } else {
      default
    }

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.goodsEntries.declarationGoodsIfComplete
      .fold(actionProvider.invalidRequestF(goodsDeclarationIncompleteMessage)) { declarationGoods =>
        request.declarationJourney.maybeGoodsDestination
          .fold(actionProvider.invalidRequestF(goodsDestinationUnansweredMessage)) { destination =>
            mibService.paymentCalculations(declarationGoods.goods, destination).map { calculationResponse =>
              (calculationResponse.thresholdCheck, request.declarationJourney.declarationType) match {
                case (OverThreshold, _)        => Redirect(GoodsOverThresholdController.onPageLoad)
                case (WithinThreshold, Import) => importView(calculationResponse.results)
                case (WithinThreshold, Export) =>
                  Redirect(checkYourAnswersIfComplete(CustomsAgentController.onPageLoad))

              }
            }
          }
      }
  }

  private def importView(calculationResults: CalculationResults)(implicit
    request: DeclarationJourneyRequest[_]
  ): Result =
    Ok(
      view(
        calculationResults,
        checkYourAnswersIfComplete(CustomsAgentController.onPageLoad),
        backButtonUrl
      )
    )
}
