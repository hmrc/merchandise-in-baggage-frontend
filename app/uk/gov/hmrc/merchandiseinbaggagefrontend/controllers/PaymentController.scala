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

import javax.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.merchandiseinbaggagefrontend.connectors.PaymentConnector
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api._
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.PaymentPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentController @Inject()(
                                   mcc: MessagesControllerComponents,
                                   paymentPage: PaymentPage,
                                   httpClient: HttpClient)(implicit val ec: ExecutionContext, appConfig: AppConfig, errorHandler: ErrorHandler)
  extends FrontendController(mcc) with PaymentConnector {

  val onPageLoad: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(paymentPage()))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    //TODO hard coded data for now
    val body = PayApitRequest(
      MibReference("MIBI1234567890"),
      AmountInPence(1),
      AmountInPence(2),
      AmountInPence(3),
      TraderDetails("Trader Inc, 239 Old Street, Berlin, Germany, EC1V 9EY"),
      MerchandiseDetails("Parts and technical crew for the forest moon")
    )
    makePayment(httpClient, body).map { response => Redirect(extractUrl(response).nextUrl.value) }
      .recoverWith {
        case _: Throwable => Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }
  }
}

