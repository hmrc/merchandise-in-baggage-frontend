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
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.SearchGoodsCountryFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.{CategoryQuantityOfGoods, Currency, CurrencyAmount, GoodsEntry, PriceOfGoods}
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.CountriesService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.SearchGoodsCountryView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchGoodsCountryController @Inject()(
                                              override val controllerComponents: MessagesControllerComponents,
                                              actionProvider: DeclarationJourneyActionProvider,
                                              formProvider: SearchGoodsCountryFormProvider,
                                              repo: DeclarationJourneyRepository,
                                              view: SearchGoodsCountryView
                                          )(implicit ec: ExecutionContext, appConfig: AppConfig) extends DeclarationJourneyUpdateController {

  val form: Form[String] = formProvider(CountriesService.countries)

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>

    val preparedForm = request.declarationJourney.goodsEntries.headOption match {
      case Some(goodsEntry) => goodsEntry.maybeCountryOfPurchase.fold(form)(form.fill)
      case None => form
    }

    Ok(view(preparedForm))
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          repo.upsert(request.declarationJourney.copy(goodsEntries = Seq(
            GoodsEntry(
              CategoryQuantityOfGoods("TODO", "123"),
              Some("TODO"),
              Some(value),
              Some(PriceOfGoods(CurrencyAmount(-1.00), Currency("made up", "ABC"))),
              Some("TODO"),
              Some(CurrencyAmount(-1.00))
            )
          ))).map { _ =>
            Redirect(routes.SkeletonJourneyController.purchaseDetails())
          }
      )
  }

}
