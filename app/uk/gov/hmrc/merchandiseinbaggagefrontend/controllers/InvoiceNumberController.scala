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
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.InvoiceNumberForm
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsEntries
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.InvoiceNumberView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InvoiceNumberController @Inject()(
                                         override val controllerComponents: MessagesControllerComponents,
                                         actionProvider: DeclarationJourneyActionProvider,
                                         repo: DeclarationJourneyRepository,
                                         view: InvoiceNumberView
                                       )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends IndexedDeclarationJourneyUpdateController {

  val form: Form[String] = InvoiceNumberForm.form

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    request.declarationJourney.goodsEntries.entries.lift(idx - 1).fold(actionProvider.invalidRequest) { goodsEntry =>
      goodsEntry.maybeCategoryQuantityOfGoods.fold(actionProvider.invalidRequest) { categoryQuantityOfGoods =>
        val preparedForm = goodsEntry.maybeInvoiceNumber.fold(form)(form.fill)

        Ok(view(preparedForm, idx, categoryQuantityOfGoods.category))
      }
    }
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.goodsEntries.entries.lift(idx - 1).fold(actionProvider.invalidRequestF) { goodsEntry =>
      goodsEntry.maybeCategoryQuantityOfGoods.fold(actionProvider.invalidRequestF) { categoryQuantityOfGoods =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, idx, categoryQuantityOfGoods.category))),
            invoiceNumber => {
              val updatedGoodsEntries =
                request.declarationJourney.goodsEntries.entries.updated(
                  idx - 1,
                  goodsEntry.copy(
                    maybeInvoiceNumber = Some(invoiceNumber),
                    maybeTaxDue = Some(BigDecimal(-999.99))
                  ))

              repo.upsert(
                request.declarationJourney.copy(
                  goodsEntries = GoodsEntries(updatedGoodsEntries)
                )
              ).map { _ =>
                Redirect(routes.ReviewGoodsController.onPageLoad())
              }
            }
          )
      }
    }
  }

}
