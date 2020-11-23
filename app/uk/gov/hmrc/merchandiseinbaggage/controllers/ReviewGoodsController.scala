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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.goodsDeclarationIncompleteMessage
import uk.gov.hmrc.merchandiseinbaggage.forms.ReviewGoodsForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{GoodsEntries, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.ReviewGoodsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReviewGoodsController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                      actionProvider: DeclarationJourneyActionProvider,
                                      override val repo: DeclarationJourneyRepository,
                                      view: ReviewGoodsView)
                                     (implicit ec: ExecutionContext, appConfig: AppConfig)
  extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]): Call =
    routes.PurchaseDetailsController.onPageLoad(request.declarationJourney.goodsEntries.entries.size)

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    request.declarationJourney.goodsEntries.declarationGoodsIfComplete
      .fold(actionProvider.invalidRequest(goodsDeclarationIncompleteMessage)) { goods =>
        Ok(view(form, goods, backButtonUrl))
      }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.goodsEntries.declarationGoodsIfComplete
      .fold(actionProvider.invalidRequestF(goodsDeclarationIncompleteMessage)) { goods =>
        form.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, goods, backButtonUrl))),
          declareMoreGoods =>
            if (declareMoreGoods == Yes) {
              val updatedGoodsEntries = request.declarationJourney.goodsEntries.entries :+ GoodsEntry.empty

              repo.upsert(request.declarationJourney.copy(goodsEntries = GoodsEntries(updatedGoodsEntries))).map { _ =>
                Redirect(routes.GoodsTypeQuantityController.onPageLoad(updatedGoodsEntries.size))
              }
            } else Future.successful(Redirect(routes.PaymentCalculationController.onPageLoad()))
        )
      }
  }
}
