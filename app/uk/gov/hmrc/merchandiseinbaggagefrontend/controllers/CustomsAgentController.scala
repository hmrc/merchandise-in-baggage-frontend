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

import javax.inject.Inject
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.CustomAgentFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.YesNo
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.CustomsAgentView

import scala.concurrent.{ExecutionContext, Future}

class CustomsAgentController @Inject()(
                                        override val controllerComponents: MessagesControllerComponents,
                                        actionProvider: DeclarationJourneyActionProvider,
                                        formProvider: CustomAgentFormProvider,
                                        repo: DeclarationJourneyRepository,
                                        view: CustomsAgentView,
                                      )(implicit ec: ExecutionContext, appConf: AppConfig) extends DeclarationJourneyUpdateController {

  override val onSubmit: Action[AnyContent] = customsAgentSubmit
  override val onPageLoad: Action[AnyContent] = customsAgent

  private val form: Form[Boolean] = formProvider()

  private def customsAgent: Action[AnyContent] = actionProvider.journeyAction { implicit request =>
    Ok(view(request.declarationJourney.maybeIsACustomsAgent.fold(form)(asw => form.fill(YesNo.to(asw)))))
  }

  private def customsAgentSubmit: Action[AnyContent] = actionProvider.journeyAction.async { implicit request =>
    def onError(): Future[Result] = Future.successful(BadRequest("something WRONG"))

    form.bindFromRequest().fold(_ => onError(), { answer =>
      repo.upsert(request.declarationJourney.copy(maybeIsACustomsAgent = Some(YesNo.from(answer)))).map { _ =>
        if (answer) Redirect(routes.SkeletonJourneyController.agentDetails())
        else Redirect(routes.SkeletonJourneyController.enterEoriNumber())
      }
    })
  }
}
