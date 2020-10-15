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

import play.api.i18n.Messages
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.ErrorHandler
import uk.gov.hmrc.merchandiseinbaggagefrontend.connectors.PaymentConnector
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.PayApiRequest
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.{ErrorTemplate, PaymentPage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PaymentControllerSpec extends DeclarationJourneyControllerSpec {

  private lazy val view = injector.instanceOf[PaymentPage]
  private lazy val httpClient = injector.instanceOf[HttpClient]
  private lazy val component = injector.instanceOf[MessagesControllerComponents]
  private lazy val errorHandlerTemplate = injector.instanceOf[ErrorTemplate]
  private implicit lazy val errorHandler: ErrorHandler = injector.instanceOf[ErrorHandler]

  private def messages[A](fakeRequest: FakeRequest[A]): Messages = messagesApi.preferred(fakeRequest)

  "on page load will render PaymentPage template" in {
    val controller =  injector.instanceOf[PaymentController]
    val getRequest = buildGet(routes.PaymentController.onPageLoad().url)

    contentAsString(controller.onPageLoad(getRequest)) mustBe view()(getRequest, messages(getRequest), appConfig).toString
  }

  "on submit will trigger a call to pay-api to make payment and render the response" in {
    val stubbedApiResponse = s"""{"journeyId":"5f3b","nextUrl":"http://host"}"""

    val testConnector = new PaymentConnector(httpClient, ""){
      override def makePayment(requestBody: PayApiRequest)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
        Future.successful(HttpResponse(201, stubbedApiResponse))
    }

    val controller = new PaymentController(component, view, testConnector)

    val postRequest = buildPost(routes.PaymentController.onSubmit().url)
      .withFormUrlEncodedBody("taxDue" -> "1011")
    val eventualResult = controller.onSubmit()(postRequest)

    status(eventualResult) mustBe 303
    redirectLocation(eventualResult) mustBe Some("http://host")
  }

  "on submit will return error page if call to pay-api fails" in {
    val testConnector = new PaymentConnector(httpClient, ""){
      override def makePayment(requestBody: PayApiRequest)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
        Future.failed(new Exception("Something wrong"))
    }

    val controller = new PaymentController(component, view, testConnector)

    val postRequest = buildPost(routes.PaymentController.onSubmit().url).withFormUrlEncodedBody("taxDue" -> "10")
    val eventualResult = controller.onSubmit()(postRequest)

    status(eventualResult) mustBe 500
    contentAsString(eventualResult) mustBe errorHandlerTemplate(
      "Sorry, we are experiencing technical difficulties - 500",
      "Sorry, we’re experiencing technical difficulties",
      "Please try again in a few minutes.")(postRequest, messages(postRequest), appConfig).toString
  }
}
