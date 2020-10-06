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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.GoodsVatRateFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsVatRate
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.GoodsVatRateView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsVatRateController @Inject()(
                                        override val controllerComponents: MessagesControllerComponents,
                                        actionProvider: DeclarationJourneyActionProvider,
                                        formProvider: GoodsVatRateFormProvider,
                                        repo: DeclarationJourneyRepository,
                                        view: GoodsVatRateView
                                          )(implicit ec: ExecutionContext, appConfig: AppConfig) extends DeclarationJourneyUpdateController {

  val form: Form[GoodsVatRate] = formProvider()

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    // TODO replace with parameterised :idx, use headOption for single goods journey
    request.declarationJourney.goodsEntries.headOption match {
      case Some(goodsEntry) =>
        Ok(view(goodsEntry.maybeGoodsVatRate.fold(form)(form.fill), goodsEntry.categoryQuantityOfGoods.category))
      case None => Redirect(routes.InvalidRequestController.onPageLoad())
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    request.declarationJourney.goodsEntries.headOption match {
      case Some(goodsEntry) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, goodsEntry.categoryQuantityOfGoods.category))),
            value =>
              repo.upsert(request.declarationJourney.copy(goodsEntries = Seq(
                goodsEntry.copy(maybeGoodsVatRate = Some(value))
              ))).map { _ =>
                Redirect(routes.SearchGoodsCountryController.onPageLoad())
              }
          )
      case None =>
        Future.successful(Redirect(routes.InvalidRequestController.onPageLoad()))
    }
  }

}
