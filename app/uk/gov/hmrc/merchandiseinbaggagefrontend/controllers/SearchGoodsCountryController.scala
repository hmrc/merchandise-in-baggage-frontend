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
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.SearchGoodsCountryForm.form
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CountriesService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.SearchGoodsCountryView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchGoodsCountryController @Inject()(
                                              override val controllerComponents: MessagesControllerComponents,
                                              actionProvider: DeclarationJourneyActionProvider,
                                              override val repo: DeclarationJourneyRepository,
                                              view: SearchGoodsCountryView
                                            )(implicit ec: ExecutionContext, appConfig: AppConfig)
  extends IndexedDeclarationJourneyUpdateController {

  private def backButtonUrl(index: Int): Call = routes.GoodsVatRateController.onPageLoad(index)

  val countriesForm: Form[String] = form(CountriesService.countries)

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      val preparedForm = request.goodsEntry.maybeCountryOfPurchase.fold(countriesForm)(countriesForm.fill)

      Future successful Ok(view(preparedForm, idx, category, backButtonUrl(idx)))
    }
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      countriesForm
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, idx, category, backButtonUrl(idx)))),
          country =>
            persistAndRedirect(
              request.goodsEntry.copy(maybeCountryOfPurchase = Some(country)),
              idx,
              routes.PurchaseDetailsController.onPageLoad(idx))
        )
    }
  }
}
