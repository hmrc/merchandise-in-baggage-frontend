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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.SearchGoodsFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{CategoryQuantityOfGoods, GoodsEntries, GoodsEntry}
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.SearchGoodsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchGoodsController @Inject()(
                                       override val controllerComponents: MessagesControllerComponents,
                                       actionProvider: DeclarationJourneyActionProvider,
                                       formProvider: SearchGoodsFormProvider,
                                       repo: DeclarationJourneyRepository,
                                       view: SearchGoodsView
                                          )(implicit ec: ExecutionContext, appConfig: AppConfig) extends DeclarationJourneyUpdateController {

  val form: Form[CategoryQuantityOfGoods] = formProvider()

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    // TODO replace with parameterised :idx, use headOption for single goods journey
    val preparedForm = request.declarationJourney.goodsEntries.entries.headOption match {
      case Some(goodsEntry) => form.fill(goodsEntry.categoryQuantityOfGoods)
      case None => form
    }

    Ok(view(preparedForm))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        categoryQuantityOfGoods =>
          repo.upsert(
            request.declarationJourney.copy(
              goodsEntries = GoodsEntries(GoodsEntry(categoryQuantityOfGoods)))).map {_ =>
            Redirect(routes.GoodsVatRateController.onPageLoad())
          }
      )
  }

}
