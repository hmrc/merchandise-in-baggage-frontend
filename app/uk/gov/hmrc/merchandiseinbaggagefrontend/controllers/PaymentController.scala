/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.model._
import uk.gov.hmrc.merchandiseinbaggagefrontend.service.PaymentService
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.PaymentPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentController @Inject()(
                                   mcc: MessagesControllerComponents,
                                   paymentPage: PaymentPage,
                                   httpClient: HttpClient)(implicit val ec: ExecutionContext, appConfig: AppConfig)
  extends FrontendController(mcc) with PaymentService {

  val onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(paymentPage()))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    //TODO hard coded data for now
    val body = PaymentRequest(
      MibReference("MIBI1234567890"),
      AmountInPence(1),
      AmountInPence(2),
      AmountInPence(3),
      TraderDetails("Trader Inc, 239 Old Street, Berlin, Germany, EC1V 9EY"),
      MerchandiseDetails("Parts and technical crew for the forest moon")
    )
    makePayment(httpClient, body).map(response => Ok(response.body))
  }
}

