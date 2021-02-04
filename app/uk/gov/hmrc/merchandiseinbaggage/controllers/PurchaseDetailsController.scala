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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.PurchaseDetailsForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Currency, PurchaseDetails}
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{ExportGoodsEntry, GoodsEntry, ImportGoodsEntry, PurchaseDetailsInput}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.service.CurrencyService
import uk.gov.hmrc.merchandiseinbaggage.views.html.{PurchaseDetailsExportView, PurchaseDetailsImportView}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PurchaseDetailsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  importView: PurchaseDetailsImportView,
  exportView: PurchaseDetailsExportView,
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends IndexedDeclarationJourneyUpdateController {

  private def backButtonUrl(index: Int)(implicit request: DeclarationGoodsRequest[_]) =
    checkYourAnswersOrReviewGoodsElse(routes.SearchGoodsCountryController.onPageLoad(index), index)

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      request.declarationJourney.declarationType match {
        case Import =>
          val preparedForm = request.goodsEntry.maybePurchaseDetails.fold(form)(p => form.fill(p.purchaseDetailsInput))

          Future successful Ok(importView(preparedForm, idx, category, backButtonUrl(idx)))
        case Export =>
          val preparedForm = request.goodsEntry.maybePurchaseDetails.fold(form)(p => form.fill(p.purchaseDetailsInput))

          Future successful Ok(exportView(preparedForm, idx, category, backButtonUrl(idx)))
      }
    }
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async {
    implicit request: DeclarationGoodsRequest[AnyContent] =>
      withGoodsCategory(request.goodsEntry) { category =>
        request.declarationJourney.declarationType match {
          case Import =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future successful BadRequest(importView(formWithErrors, idx, category, backButtonUrl(idx))),
                purchaseDetailsInput => handleSuccess(purchaseDetailsInput, idx)
              )
          case Export =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future successful BadRequest(exportView(formWithErrors, idx, category, backButtonUrl(idx))),
                purchaseDetailsInput => handleSuccess(purchaseDetailsInput, idx)
              )
        }
      }
  }

  private def handleSuccess(purchaseDetailsInput: PurchaseDetailsInput, idx: Int)(
    implicit request: DeclarationGoodsRequest[AnyContent]): Future[Result] =
    CurrencyService
      .getCurrencyByCode(purchaseDetailsInput.currency)
      .fold(actionProvider.invalidRequestF(s"currency [${purchaseDetailsInput.currency}] not found")) { currency =>
        persistAndRedirect(
          updateGoodsEntry(purchaseDetailsInput.price, currency),
          idx,
          routes.ReviewGoodsController.onPageLoad()
        )
      }

  private def updateGoodsEntry(amount: String, currency: Currency)(implicit request: DeclarationGoodsRequest[AnyContent]): GoodsEntry =
    request.goodsEntry match {
      case entry: ImportGoodsEntry => entry.copy(maybePurchaseDetails = Some(PurchaseDetails(amount, currency)))
      case entry: ExportGoodsEntry => entry.copy(maybePurchaseDetails = Some(PurchaseDetails(amount, currency)))
    }
}
