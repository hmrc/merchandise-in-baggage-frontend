/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.forms.SearchGoodsCountryForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.core.{ExportGoodsEntry, ImportGoodsEntry}
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.service.CountryService
import uk.gov.hmrc.merchandiseinbaggage.views.html.SearchGoodsCountryView
import com.softwaremill.quicklens._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchGoodsCountryController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  repo: DeclarationJourneyRepository,
  navigator: Navigator,
  view: SearchGoodsCountryView
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends IndexedDeclarationJourneyUpdateController {

  private def backButtonUrl(index: Int)(implicit request: DeclarationGoodsRequest[_]) =
    checkYourAnswersOrReviewGoodsElse(PurchaseDetailsController.onPageLoad(index), index)

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      request.goodsEntry match {
        case _: ImportGoodsEntry => Future successful Redirect(GoodsOriginController.onPageLoad(idx))
        case entry: ExportGoodsEntry =>
          val preparedForm = entry.maybeDestination
            .fold(form)(c => form.fill(c.code))

          Future successful Ok(view(preparedForm, idx, category, backButtonUrl(idx)))
      }
    }
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, idx, category, backButtonUrl(idx)))),
          countryCode =>
            CountryService
              .getCountryByCode(countryCode)
              .fold(actionProvider.invalidRequestF(s"country [$countryCode] not found")) { country =>
                navigator
                  .nextPage(
                    SearchGoodsCountryRequest(
                      request.declarationJourney,
                      request.goodsEntry.modify(_.when[ExportGoodsEntry].maybeDestination).setTo(Some(country)),
                      idx,
                      repo.upsert
                    ))
                  .map(Redirect)
            }
        )
    }
  }
}
