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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.GoodsTypeQuantityView.form
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsTypeQuantityView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsTypeQuantityController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                            actionProvider: DeclarationJourneyActionProvider,
                                            override val repo: DeclarationJourneyRepository,
                                            view: GoodsTypeQuantityView
                                           )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends IndexedDeclarationJourneyUpdateController {

  private def backButtonUrl(idx: Int)(implicit request: DeclarationGoodsRequest[_]) =
    backToCheckYourAnswersOrReviewGoodsElse(routes.ValueWeightOfGoodsController.onPageLoad(), idx)

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx) { implicit request =>
    val preparedForm = request.goodsEntry.maybeCategoryQuantityOfGoods.fold(form)(form.fill)

    Ok(view(preparedForm, idx, backButtonUrl(idx)))
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, idx, backButtonUrl(idx)))),
        categoryQuantityOfGoods =>
          persistAndRedirect(
            request.goodsEntry.copy(maybeCategoryQuantityOfGoods = Some(categoryQuantityOfGoods)),
            idx,
            routes.GoodsVatRateController.onPageLoad(idx))
      )
  }
}
