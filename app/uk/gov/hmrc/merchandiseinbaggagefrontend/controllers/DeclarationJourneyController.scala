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

import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, Request, Result}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsEntry
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.Future

trait DeclarationJourneyController extends FrontendBaseController {
  implicit def messages(implicit request: Request[_]): Messages = controllerComponents.messagesApi.preferred(request)

  val onPageLoad: Action[AnyContent]
}

trait DeclarationJourneyUpdateController extends DeclarationJourneyController {
  val onSubmit: Action[AnyContent]
}

trait IndexedDeclarationJourneyController extends FrontendBaseController {
  implicit def messages(implicit request: Request[_]): Messages = controllerComponents.messagesApi.preferred(request)

  def onPageLoad(idx: Int): Action[AnyContent]

  def withGoodsCategory(goodsEntry: GoodsEntry)(f: String => Future[Result]): Future[Result] =
    goodsEntry.maybeCategoryQuantityOfGoods match {
      case Some(c) => f(c.category)
      case None => Future successful Redirect(routes.InvalidRequestController.onPageLoad())
    }
}

trait IndexedDeclarationJourneyUpdateController extends IndexedDeclarationJourneyController {
  def onSubmit(idx: Int): Action[AnyContent]
}
