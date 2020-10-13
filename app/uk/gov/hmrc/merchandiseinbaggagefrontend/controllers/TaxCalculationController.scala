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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.TaxCalculationView

import scala.concurrent.ExecutionContext

@Singleton
class TaxCalculationController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                         actionProvider: DeclarationJourneyActionProvider,
                                         calculationService: CalculationService,
                                         view: TaxCalculationView)
                                        (implicit val appConfig: AppConfig, ec: ExecutionContext)
  extends DeclarationJourneyController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.goodsEntries.declarationGoodsIfComplete.fold(actionProvider.invalidRequestF) { goods =>
      calculationService.taxCalculation(goods).map { taxCalculations =>
        Ok(view(taxCalculations, routes.SkeletonJourneyController.customsAgent()))
      }
    }
  }
}
