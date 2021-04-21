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

import cats.data.OptionT
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.{declarationNotFoundMessage, goodsDeclarationIncompleteMessage}
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.forms.ReviewGoodsForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationResults, WithinThreshold}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.navigation.ReviewGoodsRequest
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.service.CalculationService
import uk.gov.hmrc.merchandiseinbaggage.views.html.ReviewGoodsView

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReviewGoodsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  override val repo: DeclarationJourneyRepository,
  view: ReviewGoodsView,
  calculationService: CalculationService,
  navigator: Navigator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends DeclarationJourneyUpdateController {

  private def backButtonUrl(implicit request: DeclarationJourneyRequest[_]) =
    request.declarationJourney.declarationType match {
      case Import => GoodsVatRateController.onPageLoad(request.declarationJourney.goodsEntries.entries.size)
      case Export => SearchGoodsCountryController.onPageLoad(request.declarationJourney.goodsEntries.entries.size)
    }

  val onPageLoad: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    import request.declarationJourney._
    goodsEntries.declarationGoodsIfComplete
      .fold(actionProvider.invalidRequest(goodsDeclarationIncompleteMessage)) { goods =>
        Ok(view(form, goods, backButtonUrl, declarationType, journeyType))
      }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    import request.declarationJourney._
    goodsEntries.declarationGoodsIfComplete
      .fold(actionProvider.invalidRequestF(goodsDeclarationIncompleteMessage)) { goods =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, goods, backButtonUrl, declarationType, journeyType))),
            redirectTo
          )
      }
  }

  private def redirectTo(declareMoreGoods: YesNo)(implicit request: DeclarationJourneyRequest[_]): Future[Result] =
    (for {
      check <- checkThresholdIfAmending(request.declarationJourney)
      call <- OptionT.liftF(
               navigator.nextPage(
                 ReviewGoodsRequest(
                   declareMoreGoods,
                   request.declarationJourney,
                   check.thresholdCheck,
                   repo.upsert
                 )
               ))
    } yield call).fold(actionProvider.invalidRequest(declarationNotFoundMessage))(Redirect)

  private def checkThresholdIfAmending(declarationJourney: DeclarationJourney)(
    implicit hc: HeaderCarrier): OptionT[Future, CalculationResults] =
    declarationJourney.amendmentIfRequiredAndComplete
      .fold(OptionT.pure[Future](CalculationResults(Seq.empty, WithinThreshold))) { _ =>
        overThresholdCheck(declarationJourney)
      }

  private def overThresholdCheck(declarationJourney: DeclarationJourney)(implicit hc: HeaderCarrier): OptionT[Future, CalculationResults] =
    declarationJourney.declarationType match {
      case Import => calculationService.isAmendPlusOriginalOverThresholdImport(declarationJourney)
      case Export => calculationService.isAmendPlusOriginalOverThresholdExport(declarationJourney)
    }
}
