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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.GoodsOriginForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.core.{ExportGoodsEntry, ImportGoodsEntry}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsOriginView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsOriginController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: GoodsOriginView)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends IndexedDeclarationJourneyUpdateController {

  private def backButtonUrl(index: Int)(implicit request: DeclarationGoodsRequest[_]) =
    checkYourAnswersOrReviewGoodsElse(routes.GoodsVatRateController.onPageLoad(index), index)

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      request.goodsEntry match {
        case entry: ImportGoodsEntry =>
          val preparedForm = entry.maybeProducedInEu.fold(form)(form.fill)
          Future successful Ok(view(preparedForm, idx, category, backButtonUrl(idx)))
        case _: ExportGoodsEntry =>
          Future successful Redirect(routes.SearchGoodsCountryController.onPageLoad(idx))
      }
    }
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, idx, category, backButtonUrl(idx)))),
          producedInEu =>
            persistAndRedirect(
              request.goodsEntry.asInstanceOf[ImportGoodsEntry].copy(maybeProducedInEu = Some(producedInEu)),
              idx,
              routes.PurchaseDetailsController.onPageLoad(idx))
        )
    }
  }
}
