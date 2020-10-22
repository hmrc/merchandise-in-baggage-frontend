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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.GoodsVatRateForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.GoodsVatRateView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsVatRateController @Inject()(
                                        override val controllerComponents: MessagesControllerComponents,
                                        actionProvider: DeclarationJourneyActionProvider,
                                        repo: DeclarationJourneyRepository,
                                        view: GoodsVatRateView)
                                      (implicit ec: ExecutionContext, appConfig: AppConfig)
  extends IndexedDeclarationJourneyUpdateController {

  def onPageLoad(idx: Int, change: Boolean): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      val preparedForm = request.goodsEntry.maybeGoodsVatRate.fold(form)(form.fill)

      Future successful Ok(view(preparedForm, idx, category, change))
    }
  }

  def onSubmit(idx: Int, change: Boolean): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, idx, category, change))),
          goodsVatRate => {
            repo.upsert(
              request.declarationJourney.copy(
                goodsEntries = request.declarationJourney.goodsEntries.patch(
                  idx,
                  request.goodsEntry.copy(maybeGoodsVatRate = Some(goodsVatRate))
                )
              )
            ).map { _ =>
              if(change) Redirect(routes.ReviewGoodsController.onPageLoad())
              else Redirect(routes.SearchGoodsCountryController.onPageLoad(idx, change))
            }
          }
        )
    }
  }

}
