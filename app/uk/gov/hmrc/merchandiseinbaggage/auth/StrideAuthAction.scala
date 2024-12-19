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

package uk.gov.hmrc.merchandiseinbaggage.auth

import play.api.mvc.Results.{Forbidden, Unauthorized}
import play.api.mvc.*
import play.api.{Configuration, Environment, Logging}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.retrieve.*
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StrideAuthAction @Inject() (
  override val authConnector: AuthConnector,
  appConfig: AppConfig,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends ActionBuilder[AuthRequest, AnyContent]
    with AuthorisedFunctions
    with AuthRedirects
    with Logging {

  override def parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override def config: Configuration = appConfig.config

  override def env: Environment = appConfig.env

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A], block: AuthRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    def redirectToStrideLogin(message: String) = {
      logger.warn(s"[StrideAuthAction][invokeBlock] user is not authenticated - redirecting user to login: $message")
      val uri = if (request.host.contains("localhost")) s"http://${request.host}${request.uri}" else s"${request.uri}"
      toStrideLogin(uri)
    }

    // This service handles traffic from the public internet as well as the stride domain.
    // Traffic from the stride domain can be identified by the x-forwarded-host header.
    // In the case that it's not from the stride domain, we don't need stride auth, instead
    // we invoke the block with an AuthRequest with no credentials and the isAssistedDigital
    // flag set to false

    val isFromAdminDomain: Boolean =
      request.headers
        .get("x-forwarded-host")
        .exists(host => host.startsWith("admin") || host.startsWith("test-admin"))

    if (!isFromAdminDomain) {
      block(AuthRequest(request, credentials = None, isAssistedDigital = false))
    } else {
      authorised(AuthProviders(PrivilegedApplication))
        .retrieve(credentials and allEnrolments) { case creds ~ enrolments =>
          if (hasRequiredRoles(enrolments)) {
            block(AuthRequest(request, creds, isAssistedDigital = true))
          } else {
            Future successful Unauthorized("Insufficient Roles")
          }
        }
        .recover {
          case e: NoActiveSession        =>
            redirectToStrideLogin(e.getMessage)
          case e: InternalError          =>
            redirectToStrideLogin(e.getMessage)
          case e: AuthorisationException =>
            logger.warn(s"[StrideAuthAction][invokeBlock] User is forbidden because of ${e.reason}, $e")
            Forbidden
        }
    }
  }

  private def hasRequiredRoles(enrolments: Enrolments): Boolean = {
    val requiredEnrolments = appConfig.strideRoles

    requiredEnrolments.forall(e => enrolments.getEnrolment(e).isDefined)
  }
}
