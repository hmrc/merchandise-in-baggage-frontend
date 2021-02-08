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

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.{goodsDeclarationIncompleteMessage, goodsDestinationUnansweredMessage}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsOverThresholdView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsOverThresholdController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  calculationService: CalculationService,
  view: GoodsOverThresholdView)(implicit val appConfig: AppConfig, ec: ExecutionContext)
    extends DeclarationJourneyController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.goodsEntries.declarationGoodsIfComplete
      .fold(actionProvider.invalidRequestF(goodsDeclarationIncompleteMessage)) { goods =>
        request.declarationJourney.maybeGoodsDestination
          .fold(actionProvider.invalidRequestF(goodsDestinationUnansweredMessage)) { destination =>
            request.declarationType match {
              case Import =>
                calculationService.paymentCalculations(goods.importGoods).map { calculations =>
                  import calculations._
                  Ok(
                    view(
                      destination,
                      calculations.totalGbpValue,
                      paymentCalculations.flatMap(_.calculationResult.conversionRatePeriod).distinct,
                      Import))
                }
              case Export =>
                val amount = goods.goods.map(_.purchaseDetails.numericAmount).sum.fromBigDecimal
                Future successful Ok(view(destination, amount, Seq.empty, Export))
            }
          }
      }
  }
}
