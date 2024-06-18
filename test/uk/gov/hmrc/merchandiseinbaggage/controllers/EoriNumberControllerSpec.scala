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

import org.mockito.Mockito.mock
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse
import uk.gov.hmrc.merchandiseinbaggage.views.html.EoriNumberView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EoriNumberControllerSpec extends DeclarationJourneyControllerSpec {

  val view: EoriNumberView             = injector.instanceOf[EoriNumberView]
  val client: HttpClient               = injector.instanceOf[HttpClient]
  val mockNavigator: Navigator         = mock(classOf[Navigator])
  val connector: MibConnector          = new MibConnector(appConfig, client) {
    override def checkEoriNumber(eori: String)(implicit hc: HeaderCarrier): Future[CheckResponse] =
      Future.successful(CheckResponse("123", valid = false, None))
  }
  val controller: EoriNumberController =
    new EoriNumberController(
      controllerComponents,
      actionBuilder,
      declarationJourneyRepository,
      view,
      connector,
      mockNavigator
    )

  "return an error if API EORI validation fails" in {
    givenADeclarationJourneyIsPersisted(completedDeclarationJourney)

    val result = controller.onSubmit()(
      buildPost(
        routes.EoriNumberController.onSubmit.url,
        aSessionId,
        completedDeclarationJourney,
        formData = Seq("eori" -> "GB123467800022")
      )
    )

    status(result) mustBe BAD_REQUEST
    contentAsString(result) must include(messages("eoriNumber.error.notFound"))
    contentAsString(result) must include("GB123467800022")
  }

  "return an error if API return 404" in {
    givenADeclarationJourneyIsPersisted(completedDeclarationJourney)
    val connector = new MibConnector(appConfig, client) {
      override def checkEoriNumber(eori: String)(implicit hc: HeaderCarrier): Future[CheckResponse] =
        Future.failed(new Exception("API returned 404"))
    }

    val controller: EoriNumberController =
      new EoriNumberController(
        controllerComponents,
        actionBuilder,
        declarationJourneyRepository,
        view,
        connector,
        mockNavigator
      )

    val result: Future[Result] = controller.onSubmit()(
      buildPost(
        routes.EoriNumberController.onSubmit.url,
        aSessionId,
        completedDeclarationJourney,
        formData = Seq("eori" -> "GB123467800000")
      )
    )

    status(result) mustBe BAD_REQUEST
    contentAsString(result) must include(messages("eoriNumber.error.notFound"))
  }
}
