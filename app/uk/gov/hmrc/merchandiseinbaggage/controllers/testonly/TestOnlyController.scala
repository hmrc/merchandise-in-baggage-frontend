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

package uk.gov.hmrc.merchandiseinbaggage.controllers.testonly

import java.time.LocalDate.now
import javax.inject.Inject
import play.api.data.Form
import play.api.libs.json.Json
import play.api.libs.json.Json.{prettyPrint, toJson}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.SessionKeys.sessionId
import uk.gov.hmrc.merchandiseinbaggage._
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.testonly.TestOnlyController.sampleDeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.forms.testonly.DeclarationJourneyFormProvider
import uk.gov.hmrc.merchandiseinbaggage.model.adresslookup.{Address, AddressLookupCountry}
import uk.gov.hmrc.merchandiseinbaggage.model.api.PurchaseDetails
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.core.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo._
import uk.gov.hmrc.merchandiseinbaggage.model.core.{Currency, _}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.TestOnlyDeclarationJourneyPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class TestOnlyController @Inject()(
  mcc: MessagesControllerComponents,
  repository: DeclarationJourneyRepository,
  formProvider: DeclarationJourneyFormProvider,
  page: TestOnlyDeclarationJourneyPage)(implicit val ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc) {
  private val form = formProvider()

  val displayDeclarationJourneyPage: Action[AnyContent] = Action { implicit request =>
    Ok(page(form.fill(prettyPrint(toJson(sampleDeclarationJourney(SessionId()))))))
  }

  val submitDeclarationJourneyPage: Action[AnyContent] = Action.async { implicit request =>
    def onError(form: Form[String]): Future[Result] = Future successful BadRequest(page(form))

    form
      .bindFromRequest()
      .fold(
        formWithErrors => onError(formWithErrors),
        json => {
          Json.parse(json).validate[DeclarationJourney].asOpt.fold(onError(form)) { declarationJourney =>
            repository.insert(declarationJourney).map { _ =>
              declarationJourney.declarationType match {
                case Import =>
                  Redirect(controllers.routes.GoodsDestinationController.onPageLoad())
                    .addingToSession((sessionId, declarationJourney.sessionId.value))
                case Export =>
                  Redirect(controllers.routes.GoodsDestinationController.onPageLoad())
                    .addingToSession((sessionId, declarationJourney.sessionId.value))
              }
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
      Some(GoodsVatRates.Twenty),
      Some(Country("FR", "title.france", "FR", isEu = true, Nil)),
      Some(PurchaseDetails("99.99", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))))
    )

  def sampleDeclarationJourney(sessionId: SessionId): DeclarationJourney =
    DeclarationJourney(
      sessionId = sessionId,
      declarationType = DeclarationType.Import,
      maybeExciseOrRestrictedGoods = Some(No),
      maybeImportOrExportGoodsFromTheEUViaNorthernIreland = Some(No),
      maybeGoodsDestination = Some(GreatBritain),
      maybeValueWeightOfGoodsExceedsThreshold = Some(No),
      goodsEntries = GoodsEntries(
        Seq(
          completedGoodsEntry,
          GoodsEntry(
            Some(CategoryQuantityOfGoods("cheese", "3")),
            Some(GoodsVatRates.Twenty),
            Some(Country("FR", "title.france", "FR", isEu = true, Nil)),
            Some(PurchaseDetails("199.99", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))))
          )
        )),
      maybeNameOfPersonCarryingTheGoods = Some(Name("Terry", "Test")),
      maybeEmailAddress = Some(Email("aa@test.com")),
      maybeIsACustomsAgent = Some(Yes),
      maybeCustomsAgentName = Some("Andy Agent"),
      maybeCustomsAgentAddress =
        Some(Address(Seq("1 Agent Drive", "Agent Town"), Some("AG1 5NT"), AddressLookupCountry("GB", Some("United Kingdom")))),
      maybeEori = Some(Eori("GB123467800000")),
      maybeJourneyDetailsEntry = Some(JourneyDetailsEntry("DVR", now())),
      maybeTravellingByVehicle = Some(Yes),
      maybeTravellingBySmallVehicle = Some(Yes),
      maybeRegistrationNumber = Some("T5 RRY")
    )
}
