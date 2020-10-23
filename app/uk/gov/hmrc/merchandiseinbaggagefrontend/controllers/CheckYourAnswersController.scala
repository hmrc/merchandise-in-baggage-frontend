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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.CheckYourAnswersForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.CheckYourAnswersPage

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                           actionProvider: DeclarationJourneyActionProvider,
                                           calculationService: CalculationService,
                                           page: CheckYourAnswersPage)
                                          (implicit ec: ExecutionContext, appConfig: AppConfig) extends DeclarationJourneyController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.declarationIfRequiredAndComplete.fold(actionProvider.invalidRequestF){ declaration =>
      calculationService.taxCalculation(declaration.declarationGoods).map { taxCalculations =>
        val taxDue = taxCalculations.totalTaxDue
        Ok(page(form, declaration, taxDue))
      }
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    Future.successful(Ok)
  }
}
