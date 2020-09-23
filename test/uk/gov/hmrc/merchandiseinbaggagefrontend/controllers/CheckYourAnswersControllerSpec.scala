/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpecWithApplication, CoreTestData}

class CheckYourAnswersControllerSpec extends BaseSpecWithApplication with CoreTestData {
  private lazy val controller = app.injector.instanceOf[CheckYourAnswersController]

  private val onPageLoadUrl: String = routes.CheckYourAnswersController.onPageLoad().url
  private val getRequestWithSessionId = buildGet(onPageLoadUrl, sessionId)

  override def beforeEach(): Unit = declarationJourneyRepository.deleteAll().futureValue

  private def givenADeclarationJourneyIsPersisted(declarationJourney: DeclarationJourney) =
    declarationJourneyRepository.insert(declarationJourney).futureValue

  "onPageLoad" should {
    "render the page" when {
      "a declaration has been completed" in {
        givenADeclarationJourneyIsPersisted(completedDeclarationJourney)

        val eventualResponse = controller.onPageLoad(getRequestWithSessionId)
        val content = contentAsString(eventualResponse)

        status(eventualResponse) mustBe 200

        content must include("Check your answers before making your declaration")

        content must include("Details of the goods")
        content must include("Item")
        content must include("wine")
        content must include("cheese")
        content must include("Country")
        content must include("France")
        content must include("Price")
        content must include("100.00 EUR")
        content must include("200.00 EUR")
        content must include("Tax due")
        content must include("10.00")
        content must include("20.00")

        content must include("Personal details")
        content must include("Name")
        content must include("Terry Test")
        content must include("Address")
        content must include("1 Terry Terrace, Terry Town, T11 11T")
        content must include("EORI number")
        content must include("TerrysEori")

        content must include("Journey details")
        content must include("Place of arrival")
        content must include("Dover")
        content must include("Date of arrival")

        content must include("Now send your declaration")
        content must include("I understand that:")
        content must include("I must pay Customs Duty and VAT on goods I bring them into the UK for trade or business use.")
        content must include("I will need to show my declaration and invoices if I am stopped by Border Force.")
        content must include("Warning")
        content must include("If you do not declare all your goods before entering the UK you may be fined a penalty and have your goods detained by Border Force.")
      }
    }

    "error" when {
      "no session id is set" in {
        val getRequest = buildGet(onPageLoadUrl)
        givenADeclarationJourneyIsPersisted(completedDeclarationJourney)

        intercept[Exception] {
          controller.onPageLoad(getRequest).futureValue
        }.getMessage mustBe "Unable to retrieve declaration journey"
      }

      "a declaration has not been started" in {
        intercept[Exception] {
          controller.onPageLoad(getRequestWithSessionId).futureValue
        }.getCause.getMessage mustBe "Unable to retrieve declaration journey"
      }

      "a declaration has not been completed" in {
        givenADeclarationJourneyIsPersisted(startedDeclarationJourney)

        intercept[Exception] {
          controller.onPageLoad(getRequestWithSessionId).futureValue
        }.getCause.getMessage.contains( s"incomplete declaration journey") mustBe true
      }
    }
  }
}
