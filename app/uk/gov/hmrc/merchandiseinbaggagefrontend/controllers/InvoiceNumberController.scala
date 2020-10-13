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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.InvoiceNumberForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.AmountInPence
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

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      val preparedForm = request.goodsEntry.maybeInvoiceNumber.fold(form)(form.fill)

      Future successful Ok(view(preparedForm, idx, category))
    }
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, idx, category))),
          invoiceNumber => {
            repo.upsert(
              request.declarationJourney.copy(
                goodsEntries = request.declarationJourney.goodsEntries.patch(
                  idx,
                  request.goodsEntry.copy(
                    maybeInvoiceNumber = Some(invoiceNumber)
                  )
                )
              )
            ).map { _ =>
              Redirect(routes.ReviewGoodsController.onPageLoad())
            }
          }
        )
    }
  }

}
