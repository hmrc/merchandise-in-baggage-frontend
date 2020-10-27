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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.ReviewGoodsForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{GoodsEntries, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.ReviewGoodsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReviewGoodsController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                      actionProvider: DeclarationJourneyActionProvider,
                                      repo: DeclarationJourneyRepository,
                                      view: ReviewGoodsView)
                                     (implicit ec: ExecutionContext, appConfig: AppConfig)
  extends DeclarationJourneyUpdateController {

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    request.declarationJourney.goodsEntries.declarationGoodsIfComplete.fold(actionProvider.invalidRequest) { goods =>
      Ok(view(form, goods))
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.goodsEntries.declarationGoodsIfComplete.fold(actionProvider.invalidRequestF) { goods =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, goods))),
          declareMoreGoods =>
            if (declareMoreGoods == Yes) {
              val updatedGoodsEntries = request.declarationJourney.goodsEntries.entries :+ GoodsEntry.empty

              repo.upsert(
                request.declarationJourney.copy(
                  goodsEntries = GoodsEntries(updatedGoodsEntries)
                )
              ).map { _ =>
                Redirect(routes.SearchGoodsController.onPageLoad(updatedGoodsEntries.size))
              }
            }
            else {
              Future.successful(Redirect(routes.PaymentCalculationController.onPageLoad()))
            }
        )
    }
  }
}
