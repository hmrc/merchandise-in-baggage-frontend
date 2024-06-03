/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.Inject
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc._
import uk.gov.hmrc.merchandiseinbaggage.auth.StrideAuthAction
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.Future

class LanguageSwitchController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  strideAction: StrideAuthAction
) extends FrontendBaseController
    with I18nSupport {

  private def fallbackURL: String = routes.GoodsDestinationController.onPageLoad.url

  def switchToLanguage(language: String): Action[AnyContent] = strideAction.async { implicit request =>
    val languageToUse = Lang(language)

    val redirectURL = request.headers.get(REFERER).getOrElse(fallbackURL)
    Future.successful(Redirect(redirectURL).withLang(languageToUse))
  }
}
