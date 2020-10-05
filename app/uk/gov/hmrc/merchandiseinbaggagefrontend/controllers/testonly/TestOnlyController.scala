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

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers.testonly

import java.time.LocalDate.now

import javax.inject.Inject
import play.api.data.Form
import play.api.libs.json.Json
import play.api.libs.json.Json.{prettyPrint, toJson}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.SessionKeys.sessionId
import uk.gov.hmrc.merchandiseinbaggagefrontend._
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.testonly.DeclarationJourneyFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration._
import uk.gov.hmrc.merchandiseinbaggagefrontend.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.TestOnlyDeclarationJourneyPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class TestOnlyController @Inject()(mcc: MessagesControllerComponents,
                                   repository: DeclarationJourneyRepository,
                                   formProvider: DeclarationJourneyFormProvider,
                                   page: TestOnlyDeclarationJourneyPage)
                                  (implicit val ec: ExecutionContext, appConfig: AppConfig) extends FrontendController(mcc) {
  private val form = formProvider()

  val displayDeclarationJourneyPage: Action[AnyContent] = Action { implicit request =>
    val sampleDeclarationJourney: DeclarationJourney =
      DeclarationJourney(
        sessionId = SessionId(),
        goodsEntries = Seq(
          GoodsEntry(
            "wine",
            Some("France"),
            Some(PriceOfGoods(CurrencyAmount(BigDecimal(100.00)), Currency("Euros", "EUR"))),
            Some(CurrencyAmount(BigDecimal(10.00)))),
          GoodsEntry(
            "cheese",
            Some("France"),
            Some(PriceOfGoods(CurrencyAmount(BigDecimal(200.00)), Currency("Euros", "EUR"))),
            Some(CurrencyAmount(BigDecimal(20.00))))),
        maybeName = Some(Name("Terry", "Test")),
        maybeAddress = Some(Address("1 Terry Terrace", "Terry Town", "T11 11T")),
        maybeEori = Some(Eori("TerrysEori")),
        maybeJourneyDetails = Some(JourneyDetails("Dover", now()))
      )

    Ok(page(form.fill(prettyPrint(toJson(sampleDeclarationJourney)))))
  }

  val submitDeclarationJourneyPage: Action[AnyContent] = Action.async { implicit request =>
    def onError(form: Form[String]) = Future successful BadRequest(page(form))

    form.bindFromRequest().fold(
      formWithErrors => onError(formWithErrors),
      json => {
        Json.parse(json).validate[DeclarationJourney].asOpt.fold(onError(form)) { declarationJourney =>
          repository.insert(declarationJourney).map { _ =>
            Redirect(controllers.routes.StartController.onStartImport())
              .addingToSession((sessionId, declarationJourney.sessionId.value))
          }
        }
      }
    )
  }
}
