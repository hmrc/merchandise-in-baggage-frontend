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
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.GoodsTypeQuantityView.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.GoodsTypeQuantityView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsTypeQuantityController @Inject()(override val controllerComponents: MessagesControllerComponents,
                                            actionProvider: DeclarationJourneyActionProvider,
                                            repo: DeclarationJourneyRepository,
                                            view: GoodsTypeQuantityView
                                           )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends IndexedDeclarationJourneyUpdateController {

  private val backButtonUrl: Call = routes.ValueWeightOfGoodsController.onPageLoad()

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx) { implicit request =>
    val preparedForm = request.goodsEntry.maybeCategoryQuantityOfGoods.fold(form)(form.fill)

    Ok(view(preparedForm, idx, backButtonUrl))
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, idx, backButtonUrl))),
        categoryQuantityOfGoods => {
          repo.upsert(
            request.declarationJourney.copy(
              goodsEntries = request.declarationJourney.goodsEntries.patch(
                idx,
                request.goodsEntry.copy(maybeCategoryQuantityOfGoods = Some(categoryQuantityOfGoods))
              )
            )
          ).map(_ => reviewGoodsIfCompleteElse(routes.GoodsVatRateController.onPageLoad(idx)))
        }
      )
  }
}
