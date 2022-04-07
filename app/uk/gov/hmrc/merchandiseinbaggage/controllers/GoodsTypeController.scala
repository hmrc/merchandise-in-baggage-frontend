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
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.GoodsTypeForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.Amend
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.GoodsTypeView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsTypeController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  repo: DeclarationJourneyRepository,
  view: GoodsTypeView,
  navigator: Navigator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends IndexedDeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationGoodsRequest[_]): Call = {
    val referer: String = request.headers.get(REFERER).getOrElse("")
    if (referer.contains(routes.CheckYourAnswersController.onPageLoad.url)) routes.CheckYourAnswersController.onPageLoad
    else if (referer.contains(routes.ReviewGoodsController.onPageLoad.url)) routes.ReviewGoodsController.onPageLoad
    else if (request.declarationJourney.journeyType == Amend) routes.ExciseAndRestrictedGoodsController.onPageLoad
    else routes.ValueWeightOfGoodsController.onPageLoad
  }

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx) { implicit request =>
    val preparedForm = request.goodsEntry.maybeCategory.fold(form)(form.fill)

    Ok(view(preparedForm, idx, request.declarationJourney.declarationType, request.declarationJourney.journeyType, backButtonUrl))
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(
            view(formWithErrors, idx, request.declarationJourney.declarationType, request.declarationJourney.journeyType, backButtonUrl))),
        category => {
          navigator
            .nextPage(GoodsTypeRequest(request.declarationJourney, request.goodsEntry, idx, category, repo.upsert))
            .map(Redirect)
        }
      )
  }
}
