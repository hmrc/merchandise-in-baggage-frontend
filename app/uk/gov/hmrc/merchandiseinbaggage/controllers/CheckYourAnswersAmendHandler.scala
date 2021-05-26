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
import cats.instances.future._
import com.google.inject.{Inject, Singleton}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyController.declarationNotFoundMessage
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.forms.CheckYourAnswersForm.form
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.OverThreshold
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, Declaration, DeclarationId, YesNo}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.service.{Auditor, CalculationService, PaymentService}
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
import uk.gov.hmrc.merchandiseinbaggage.utils.Utils.FutureOps
import uk.gov.hmrc.merchandiseinbaggage.views.html.{CheckYourAnswersAmendExportView, CheckYourAnswersAmendImportView}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckYourAnswersAmendHandler @Inject()(
  actionProvider: DeclarationJourneyActionProvider,
  calculationService: CalculationService,
  paymentService: PaymentService,
  val auditConnector: AuditConnector,
  val messagesApi: MessagesApi,
  amendImportView: CheckYourAnswersAmendImportView,
  amendExportView: CheckYourAnswersAmendExportView)(implicit val ec: ExecutionContext, val appConfig: AppConfig)
    extends Auditor {

  def onPageLoad(declarationJourney: DeclarationJourney, amendment: Amendment, isAgent: YesNo)(
    implicit hc: HeaderCarrier,
    request: Request[_],
    messages: Messages): Future[Result] =
    (for {
      amendPlusOriginal <- calculationService.amendPlusOriginalCalculations(declarationJourney)
      goods             <- OptionT.fromOption(declarationJourney.goodsEntries.declarationGoodsIfComplete)
      destination       <- OptionT.fromOption(declarationJourney.maybeGoodsDestination)
      outstanding       <- OptionT.liftF(calculationService.paymentCalculations(goods.goods, destination))
    } yield
      (declarationJourney.declarationType, outstanding.thresholdCheck) match {
        case (_, OverThreshold) => Redirect(GoodsOverThresholdController.onPageLoad())
        case (Import, _)        => Ok(amendImportView(form, amendment, amendPlusOriginal.results, outstanding.results.totalTaxDue, isAgent))
        case (Export, _)        => Ok(amendExportView(form, amendment, isAgent))
      }).value.map(mayBeResult => mayBeResult.fold(actionProvider.invalidRequest(declarationNotFoundMessage))(r => r))

  def onSubmit(declarationId: DeclarationId, newAmendment: Amendment)(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] =
    calculationService.findDeclaration(declarationId).flatMap { maybeOriginalDeclaration =>
      maybeOriginalDeclaration.fold(actionProvider.invalidRequest(declarationNotFoundMessage).asFuture) { originalDeclaration =>
        originalDeclaration.declarationType match {
          case Export =>
            persistAndRedirect(newAmendment, originalDeclaration)
          case Import =>
            persistAndRedirectToPayments(newAmendment, originalDeclaration)
        }
      }
    }

  private def persistAndRedirect(amendment: Amendment, originalDeclaration: Declaration)(implicit hc: HeaderCarrier): Future[Result] = {
    val amendedDeclaration = originalDeclaration.copy(amendments = originalDeclaration.amendments :+ amendment)
    calculationService.amendDeclaration(amendedDeclaration).map(_ => Redirect(DeclarationConfirmationController.onPageLoad()))
  }

  private def persistAndRedirectToPayments(amendment: Amendment, originalDeclaration: Declaration)(
    implicit hc: HeaderCarrier): Future[Result] =
    calculationService.paymentCalculations(amendment.goods.goods, originalDeclaration.goodsDestination).flatMap { calculationResults =>
      val amendmentRef = originalDeclaration.amendments.size + 1
      val updatedAmendment =
        amendment.copy(reference = amendmentRef, maybeTotalCalculationResult = Some(calculationResults.results.totalCalculationResult))

      val updatedDeclaration = originalDeclaration.copy(amendments = originalDeclaration.amendments :+ updatedAmendment)

      for {
        _ <- calculationService.amendDeclaration(updatedDeclaration)
        redirectUrl <- paymentService
                        .sendPaymentRequest(updatedDeclaration.mibReference, Some(updatedAmendment.reference), calculationResults.results)
        _ <- auditDeclaration(updatedDeclaration)
      } yield Redirect(redirectUrl)
    }
}
