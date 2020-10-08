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
import uk.gov.hmrc.merchandiseinbaggagefrontend.controllers.testonly.TestOnlyController.sampleDeclarationJourney
import uk.gov.hmrc.merchandiseinbaggagefrontend.forms.testonly.DeclarationJourneyFormProvider
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsDestination.NorthernIreland
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.PlacesOfArrival.Dover
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.Currency
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
    Ok(page(form.fill(prettyPrint(toJson(sampleDeclarationJourney(SessionId()))))))
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

object TestOnlyController {
  val completedGoodsEntry: GoodsEntry =
    GoodsEntry(
      Some(CategoryQuantityOfGoods("wine", "1")),
      Some(GoodsVatRate.Twenty),
      Some("France"),
      Some(PurchaseDetails("99.99", Currency("Eurozone", "Euro", "EUR"))),
      Some("1234560"),
      Some(BigDecimal(10.11)))

  def sampleDeclarationJourney(sessionId: SessionId): DeclarationJourney =
    DeclarationJourney(
      sessionId = sessionId,
      maybeExciseOrRestrictedGoods = Some(false),
      maybeGoodsDestination = Some(NorthernIreland),
      maybeValueWeightOfGoodsExceedsThreshold = Some(false),
      goodsEntries = GoodsEntries(
        Seq(
          completedGoodsEntry,
          GoodsEntry(
            Some(CategoryQuantityOfGoods("cheese", "3")),
            Some(GoodsVatRate.Twenty),
            Some("France"),
            Some(PurchaseDetails("199.99", Currency("Eurozone", "Euro", "EUR"))),
            Some("1234560"),
            Some(BigDecimal(20.00))))),
      maybeNameOfPersonCarryingTheGoods = Some(Name("Terry", "Test")),
      maybeIsACustomsAgent = Some(true),
      maybeCustomsAgentName = Some("Andy Agent"),
      maybeCustomsAgentAddress = Some(Address("1 Agent Drive", "Agent Town", "AG1 5NT")),
      maybeEori = Some(Eori("TerrysEori")),
      maybeJourneyDetails = Some(JourneyDetails(Dover, now())),
      maybeTravellingByVehicle = Some(true),
      maybeTravellingBySmallVehicle = Some(true),
      maybeRegistrationNumber = Some("T5 RRY")
    )
}
