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
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.connectors.CurrencyConversionConnector
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.PurchaseDetailsFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{GoodsEntries, PurchaseDetails, PurchaseDetailsInput}
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.PurchaseDetailsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PurchaseDetailsController @Inject()(
                                           override val controllerComponents: MessagesControllerComponents,
                                           override val httpClient: HttpClient,
                                           actionProvider: DeclarationJourneyActionProvider,
                                           formProvider: PurchaseDetailsFormProvider,
                                           repo: DeclarationJourneyRepository,
                                           view: PurchaseDetailsView
                                         )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends DeclarationJourneyUpdateController with CurrencyConversionConnector {

  val form: Form[PurchaseDetailsInput] = formProvider()

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    // TODO replace with parameterised :idx, use headOption for single goods journey
    request.declarationJourney.goodsEntries.entries.headOption match {
      case Some(goodsEntry) =>

        getCurrencies().map { currencyPeriod =>
          val preparedForm = goodsEntry.maybePurchaseDetails match {
            case Some(purchaseDetails) => form.fill(purchaseDetails.purchaseDetailsInput)
            case None => form
          }

          Ok(view(preparedForm, goodsEntry.goodsCategoryOrDefault, currencyPeriod.currencies))
        }
      case None => Future.successful(actionProvider.invalidRequest)
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.goodsEntries.entries.headOption match {
      case Some(goodsEntry) =>
        getCurrencies().flatMap { currencyPeriod =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, goodsEntry.goodsCategoryOrDefault, currencyPeriod.currencies))),
              value => {
                currencyPeriod.currencies.find(_.currencyCode == value.currency) match {
                  case Some(currency) =>
                    val purchaseDetails = PurchaseDetails(value.price, currency)

                    repo.upsert(
                      request.declarationJourney.copy(
                        goodsEntries = GoodsEntries(
                          goodsEntry.copy(maybePurchaseDetails = Some(purchaseDetails))))).map { _ =>
                      Redirect(routes.InvoiceNumberController.onPageLoad())
                    }
                  case None => Future.successful(actionProvider.invalidRequest)
                }
              }
            )
        }
      case None =>
        Future.successful(actionProvider.invalidRequest)
    }
  }

}
