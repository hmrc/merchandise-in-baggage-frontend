/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpecWithApplication
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.PayApitRequest
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.PaymentPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class PaymentControllerSpec extends BaseSpecWithApplication {

  val view = app.injector.instanceOf[PaymentPage]
  val httpClient = app.injector.instanceOf[HttpClient]
  val controller = new PaymentController(component, view, httpClient)

  "on page load will render PaymentPage template" in {
    val controller = new PaymentController(component, view, httpClient)
    val getRequest = buildGet(routes.PaymentController.onPageLoad().url)

    contentAsString(controller.onPageLoad(getRequest)) mustBe view()(getRequest, messages(getRequest), appConfig).toString
  }

  "on submit will trigger a call to pay-api" in {
    val stubbedApiResponse = s"""{"journeyId":"5f3b","nextUrl":"http://host"}"""
    val controller = new PaymentController(component, view, httpClient) {
      override def makePayment(httpClient: HttpClient, requestBody: PayApitRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
        Future.successful(HttpResponse(201, stubbedApiResponse))
    }

    val getRequest = buildGet(routes.PaymentController.onSubmit().url)

    contentAsString(controller.onSubmit()(getRequest)) mustBe stubbedApiResponse
  }

  "on submit will return error page if call to pay-api fails" in {
    val controller = new PaymentController(component, view, httpClient) {
      override def makePayment(httpClient: HttpClient, requestBody: PayApitRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
        Future.failed(new Exception("Something wrong"))
    }

    val getRequest = buildGet(routes.PaymentController.onSubmit().url)
    val eventualResult = controller.onSubmit()(getRequest)

    status(eventualResult) mustBe 500
    contentAsString(eventualResult) mustBe errorHandlerTemplate(
      "Sorry, we are experiencing technical difficulties - 500",
      "Sorry, we’re experiencing technical difficulties",
      "Please try again in a few minutes.")(getRequest, messages(getRequest), appConfig).toString
  }
}
