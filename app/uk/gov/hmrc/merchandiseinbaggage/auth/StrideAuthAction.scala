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

import org.slf4j.LoggerFactory.getLogger
import play.api.mvc.Results.{Forbidden, Unauthorized}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StrideAuthAction @Inject() (
  override val authConnector: AuthConnector,
  appConfig: AppConfig,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends ActionBuilder[AuthRequest, AnyContent]
    with AuthorisedFunctions
    with AuthRedirects {

  override def parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override def config: Configuration = appConfig.config

  override def env: Environment = appConfig.env

  override protected def executionContext: ExecutionContext = ec

  private val logger = getLogger(getClass)

  override def invokeBlock[A](request: Request[A], block: AuthRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    def redirectToStrideLogin(message: String) = {
      logger.warn(s"user is not authenticated - redirecting user to login: $message")
      val uri = if (request.host.contains("localhost")) s"http://${request.host}${request.uri}" else s"${request.uri}"
      toStrideLogin(uri)
    }

    authorised(AuthProviders(PrivilegedApplication))
      .retrieve(credentials and allEnrolments) { case creds ~ enrolments =>
        if (hasRequiredRoles(enrolments)) {
          block(AuthRequest(request, creds))
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
          logger.warn(s"User is forbidden because of ${e.reason}, $e")
          Forbidden
      }
  }

  private def hasRequiredRoles(enrolments: Enrolments): Boolean = {
    val requiredEnrolments = appConfig.strideRoles

    requiredEnrolments.forall(e => enrolments.getEnrolment(e).isDefined)
  }
}
