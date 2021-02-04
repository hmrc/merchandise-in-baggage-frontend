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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.connectors.MibConnector
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse
import uk.gov.hmrc.merchandiseinbaggage.views.html.EoriNumberView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EoriNumberControllerSpec extends DeclarationJourneyControllerSpec {

  val view = injector.instanceOf[EoriNumberView]
  val client = injector.instanceOf[HttpClient]
  val connector = new MibConnector(client, "some url") {
    override def checkEoriNumber(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CheckResponse] =
      Future.successful(CheckResponse("123", false, None))
  }
  val controller = new EoriNumberController(controllerComponents, actionBuilder, repository, view, connector)

  "return an error if API EROI validation fails" in {
    givenADeclarationJourneyIsPersisted(completedDeclarationJourney)

    val result = controller.onSubmit()(
      buildPost(routes.EoriNumberController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody(("eori", "GB123467800022"))
    )

    status(result) mustBe 400
    contentAsString(result) must include(messages("eoriNumber.error.notFound"))
    contentAsString(result) must include(messages("GB123467800022"))
  }

  "return an error if API return 404" in {
    givenADeclarationJourneyIsPersisted(completedDeclarationJourney)
    val connector = new MibConnector(client, "some url") {
      override def checkEoriNumber(eori: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[CheckResponse] =
        Future.failed(new Exception("API returned 404"))
    }
    val controller = new EoriNumberController(controllerComponents, actionBuilder, repository, view, connector)

    val result = controller.onSubmit()(
      buildPost(routes.EoriNumberController.onSubmit().url, aSessionId)
        .withFormUrlEncodedBody(("eori", "GB123467800000"))
    )

    status(result) mustBe 400
    contentAsString(result) must include(messages("eoriNumber.error.notFound"))
  }
}
