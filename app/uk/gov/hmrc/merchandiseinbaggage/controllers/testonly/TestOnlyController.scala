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

package uk.gov.hmrc.merchandiseinbaggage.controllers.testonly

import play.api.Logging
import play.api.data.Form
import play.api.libs.json.Json.{prettyPrint, toJson}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import uk.gov.hmrc.http.SessionKeys.sessionId
import uk.gov.hmrc.merchandiseinbaggage._
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.testonly.TestOnlyController.sampleDeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.forms.testonly.DeclarationJourneyFormProvider
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.GreatBritain
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo._
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.{Address, AddressLookupCountry}
import uk.gov.hmrc.merchandiseinbaggage.model.core.{DeclarationJourney, GoodsEntries, ImportGoodsEntry}
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.TestOnlyDeclarationJourneyPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDate.now
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyController @Inject() (
  mcc: MessagesControllerComponents,
  repository: DeclarationJourneyRepository,
  formProvider: DeclarationJourneyFormProvider,
  page: TestOnlyDeclarationJourneyPage
)(implicit val ec: ExecutionContext, appConfig: AppConfig)
    extends FrontendController(mcc)
    with Logging {
  private val form = formProvider()

  def isFromAdminDomain()(implicit request: Request[_]): Boolean =
    request.headers
      .get("x-forwarded-host")
      .exists(host => host.startsWith("admin") || host.startsWith("test-admin"))

  val displayDeclarationJourneyPage: Action[AnyContent] = Action { implicit request =>
    Ok(
      page(
        form.fill(prettyPrint(toJson(sampleDeclarationJourney(SessionId(), isFromAdminDomain())))),
        isFromAdminDomain()
      )
    )
  }

  val submitDeclarationJourneyPage: Action[AnyContent] = Action.async { implicit request =>
    def onError(form: Form[String]): Future[Result] = Future successful BadRequest(page(form, isFromAdminDomain()))

    form
      .bindFromRequest()
      .fold(
        formWithErrors => onError(formWithErrors),
        json =>
          Json.parse(json).validate[DeclarationJourney] match {
            case JsError(errors)                  =>
              logger.error(s"[TestOnlyController][submitDeclarationJourneyPage] Provided Json was invalid: $errors")
              onError(form)
            case JsSuccess(declarationJourney, _) =>
              repository.upsert(declarationJourney).map { _ =>
                declarationJourney.declarationType match {
                  case Import =>
                    Redirect(controllers.routes.GoodsDestinationController.onPageLoad)
                      .addingToSession((sessionId, declarationJourney.sessionId.value))
                  case Export =>
                    Redirect(controllers.routes.GoodsDestinationController.onPageLoad)
                      .addingToSession((sessionId, declarationJourney.sessionId.value))
                }
              }
          }
      )
  }
}

object TestOnlyController {
  val completedGoodsEntry: ImportGoodsEntry =
    ImportGoodsEntry(
      Some("wine"),
      Some(GoodsVatRates.Twenty),
      Some(YesNoDontKnow.Yes),
      Some(PurchaseDetails("99.99", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))))
    )

  def sampleDeclarationJourney(sessionId: SessionId, isAssistedDigital: Boolean): DeclarationJourney =
    DeclarationJourney(
      sessionId = sessionId,
      declarationType = DeclarationType.Import,
      isAssistedDigital = isAssistedDigital,
      maybeExciseOrRestrictedGoods = Some(No),
      maybeGoodsDestination = Some(GreatBritain),
      maybeValueWeightOfGoodsBelowThreshold = Some(Yes),
      goodsEntries = GoodsEntries(
        Seq(
          completedGoodsEntry,
          ImportGoodsEntry(
            Some("cheese"),
            Some(GoodsVatRates.Twenty),
            Some(YesNoDontKnow.Yes),
            Some(PurchaseDetails("199.99", Currency("EUR", "title.euro_eur", Some("EUR"), List("Europe", "European"))))
          )
        )
      ),
      maybeNameOfPersonCarryingTheGoods = Some(Name("Terry", "Test")),
      maybeEmailAddress = Some(Email("aa@test.com")),
      maybeIsACustomsAgent = Some(Yes),
      maybeCustomsAgentName = Some("Andy Agent"),
      maybeCustomsAgentAddress = Some(
        Address(Seq("1 Agent Drive", "Agent Town"), Some("AG1 5NT"), AddressLookupCountry("GB", Some("United Kingdom")))
      ),
      maybeEori = Some(Eori("GB123467800000")),
      maybeJourneyDetailsEntry = Some(JourneyDetailsEntry("DVR", now())),
      maybeTravellingByVehicle = Some(Yes),
      maybeTravellingBySmallVehicle = Some(Yes),
      maybeRegistrationNumber = Some("T5 RRY")
    )
}
