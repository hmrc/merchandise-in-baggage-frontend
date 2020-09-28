/*
 * Copyright 2020 HM Revenue & Customs
 *
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
            Some(PriceOfGoods(CurrencyAmount(BigDecimal(100.00)), "EUR")),
            Some(CurrencyAmount(BigDecimal(10.00)))),
          GoodsEntry(
            "cheese",
            Some("France"),
            Some(PriceOfGoods(CurrencyAmount(BigDecimal(200.00)), "EUR")),
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
            Redirect(controllers.routes.StartController.onPageLoad())
              .addingToSession((sessionId, declarationJourney.sessionId.value))
          }
        }
      }
    )
  }
}
