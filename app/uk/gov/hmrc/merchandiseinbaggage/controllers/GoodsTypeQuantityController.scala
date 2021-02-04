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
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.GoodsTypeQuantityForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{ExportGoodsEntry, ImportGoodsEntry}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsTypeQuantityView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsTypeQuantityController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: GoodsTypeQuantityView)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends IndexedDeclarationJourneyUpdateController {

  private def backButtonUrl(idx: Int)(implicit request: DeclarationGoodsRequest[_]) =
    if (request.declarationJourney.declarationRequiredAndComplete) routes.CheckYourAnswersController.onPageLoad()
    else if (request.declarationJourney.goodsEntries.declarationGoodsComplete) routes.ReviewGoodsController.onPageLoad()
    else routes.ValueWeightOfGoodsController.onPageLoad()

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx) { implicit request =>
    val preparedForm = request.goodsEntry.maybeCategoryQuantityOfGoods.fold(form)(form.fill)

    Ok(view(preparedForm, idx, request.declarationJourney.declarationType, backButtonUrl(idx)))
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, idx, request.declarationJourney.declarationType, backButtonUrl(idx)))),
        categoryQuantityOfGoods => {
          val updatedGoodsEntry = request.goodsEntry match {
            case entry: ImportGoodsEntry => entry.copy(maybeCategoryQuantityOfGoods = Some(categoryQuantityOfGoods))
            case entry: ExportGoodsEntry => entry.copy(maybeCategoryQuantityOfGoods = Some(categoryQuantityOfGoods))
          }

          val next: Call = request.declarationType match {
            case Import => routes.GoodsVatRateController.onPageLoad(idx)
            case Export => routes.SearchGoodsCountryController.onPageLoad(idx)
          }

          persistAndRedirect(updatedGoodsEntry, idx, next)
        }
      )
  }
}
