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
import uk.gov.hmrc.merchandiseinbaggagefrontend.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.PurchaseDetailsForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.PurchaseDetails
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.PurchaseDetailsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PurchaseDetailsController @Inject()(
                                           override val controllerComponents: MessagesControllerComponents,
                                           connector: CurrencyConversionConnector,
                                           actionProvider: DeclarationJourneyActionProvider,
                                           repo: DeclarationJourneyRepository,
                                           view: PurchaseDetailsView
                                         )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends IndexedDeclarationJourneyUpdateController {

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      connector.getCurrencies().map { currencyPeriod =>
        val preparedForm = request.goodsEntry.maybePurchaseDetails.fold(form)(p => form.fill(p.purchaseDetailsInput))

        Ok(view(preparedForm, idx, category, currencyPeriod.currencies))
      }
    }
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      connector.getCurrencies().flatMap { currencyPeriod =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, idx, category, currencyPeriod.currencies))),
            purchaseDetailsInput => {
              currencyPeriod.currencies.find(_.currencyCode == purchaseDetailsInput.currency)
                .fold(actionProvider.invalidRequestF) { currency =>
                  val purchaseDetails = PurchaseDetails(purchaseDetailsInput.price, currency)

                  repo.upsert(
                    request.declarationJourney.copy(
                      goodsEntries = request.declarationJourney.goodsEntries.patch(
                        idx,
                        request.goodsEntry.copy(maybePurchaseDetails = Some(purchaseDetails))
                      )
                    )
                  ).map { _ =>
                    Redirect(routes.InvoiceNumberController.onPageLoad(idx))
                  }
                }
            }
          )
      }
    }
  }

}
