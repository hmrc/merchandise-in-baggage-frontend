/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.forms.RemoveGoodsForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.Yes
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.RemoveGoodsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RemoveGoodsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: RemoveGoodsView
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends IndexedDeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationGoodsRequest[_]): Call =
    request.declarationJourney.declarationIfRequiredAndComplete
      .fold(routes.ReviewGoodsController.onPageLoad())(_ => routes.CheckYourAnswersController.onPageLoad())

  def onPageLoad(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      Future successful Ok(view(form, idx, category, request.declarationType, backButtonUrl))
    }
  }

  def onSubmit(idx: Int): Action[AnyContent] = actionProvider.goodsAction(idx).async { implicit request =>
    withGoodsCategory(request.goodsEntry) { category =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, idx, category, request.declarationType, backButtonUrl))),
          removeGoods => removeGoodOrRedirect(idx, request.declarationJourney, removeGoods)
        )
    }
  }

  def removeGoodOrRedirect(idx: Int, declarationJourney: DeclarationJourney, removeGoods: YesNo): Future[Result] =
    if (removeGoods == Yes)
      repo
        .upsert(declarationJourney.copy(goodsEntries = declarationJourney.goodsEntries.remove(idx)))
        .flatMap { _ =>
          redirectIfGoodRemoved(declarationJourney)
        } else
      backToCheckYourAnswersIfJourneyCompleted(declarationJourney)

  private def redirectIfGoodRemoved(declarationJourney: DeclarationJourney): Future[Result] =
    if (declarationJourney.goodsEntries.entries.size == 1)
      Future successful Redirect(routes.GoodsRemovedController.onPageLoad())
    else backToCheckYourAnswersIfJourneyCompleted(declarationJourney)

  private def backToCheckYourAnswersIfJourneyCompleted(declarationJourney: DeclarationJourney): Future[Result] =
    if (declarationJourney.declarationRequiredAndComplete)
      Future successful Redirect(routes.CheckYourAnswersController.onPageLoad())
    else Future successful Redirect(routes.ReviewGoodsController.onPageLoad())
}
