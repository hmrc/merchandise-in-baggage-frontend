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
import uk.gov.hmrc.merchandiseinbaggage.forms.RemoveGoodsForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo.Yes
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.RemoveGoodsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RemoveGoodsController @Inject()(
                                       override val controllerComponents: MessagesControllerComponents,
                                       actionProvider: DeclarationJourneyActionProvider,
                                       override val repo: DeclarationJourneyRepository,
                                       view: RemoveGoodsView
                                     )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends IndexedDeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationGoodsRequest[_]): Call =
    request.declarationJourney.declarationIfRequiredAndComplete
      .fold(routes.ReviewGoodsController.onPageLoad())(_ => routes.CheckYourAnswersController.onPageLoad())

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      Future successful Ok(view(form, idx, category, request.declarationType, backButtonUrl))
    }
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, idx, category, request.declarationType, backButtonUrl))),
          removeGoods => {
            if (removeGoods == Yes) {
              repo.upsert(
                request.declarationJourney.copy(
                  goodsEntries = request.declarationJourney.goodsEntries.remove(idx)
                )
              ).map { _ =>
                if (request.declarationJourney.goodsEntries.entries.size == 1) Redirect(routes.GoodsRemovedController.onPageLoad())
                else if (request.declarationJourney.declarationRequiredAndComplete) Redirect(routes.CheckYourAnswersController.onPageLoad())
                else Redirect(routes.ReviewGoodsController.onPageLoad())
              }
            } else {
              Future successful Redirect(routes.ReviewGoodsController.onPageLoad())
            }
          }
        )
    }
  }

}
