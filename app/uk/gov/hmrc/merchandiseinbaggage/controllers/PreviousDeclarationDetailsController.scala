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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, DeclarationId, Goods}
import uk.gov.hmrc.merchandiseinbaggage.views.html.PreviousDeclarationDetailsView
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import java.time.LocalDate

@Singleton
class PreviousDeclarationDetailsController @Inject()(
  override val controllerComponents: MessagesControllerComponents,
  actionProvider: DeclarationJourneyActionProvider,
  mibConnector: MibConnector,
  view: PreviousDeclarationDetailsView)(implicit ec: ExecutionContext, appConf: AppConfig)
    extends DeclarationJourneyController {

  val declarationNotFoundMessage = "original declaration was not found"

  val onPageLoad: Action[AnyContent] = actionProvider.amendAction.async { implicit request =>
    // TODO: fix to match what is being provided
    val declarationId = DeclarationId(request.session.get("declarationId").getOrElse("ERROR")) //request.declarationJourney.declarationId
    mibConnector.findDeclaration(declarationId).map {
      case Some(declaration) =>
        val now = LocalDate.now
        val travelDate = declaration.journeyDetails.dateOfTravel
        val goods = listGoods(declaration.declarationGoods.goods, declaration.amendments)
        Ok(view(goods, declaration.journeyDetails, declaration.declarationType, withinTimerange(travelDate, now)))
      case _ =>
        actionProvider.invalidRequest(s"declaration not found for id:${declarationId.value}")
    }
  }

  val onSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    Future.successful(Redirect(ExciseAndRestrictedGoodsController.onPageLoad))
  }

  def listGoods(goods: Seq[Goods], amendments: Seq[Amendment]): Seq[Goods] = {
    def paidAmendment: Seq[Goods] = amendments.filterNot(_.paymentStatus.isEmpty) flatMap (a => a.goods.goods)

    goods ++ paidAmendment
  }

  def withinTimerange(travelDate: LocalDate, now: LocalDate): Boolean = {
    val startOfRange = travelDate.minusDays(5)
    val endOfRange = travelDate.plusDays(30)

    if (now.isAfter(startOfRange) && now.isBefore(endOfRange)) true else false
  }
}
