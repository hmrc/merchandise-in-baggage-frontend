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

package uk.gov.hmrc.merchandiseinbaggage.service

import play.api.Logging
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait Auditor extends Logging {
  val auditConnector: AuditConnector

  def auditDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val eventType = "DeclarationPaymentAttempted"
    auditConnector
      .sendExtendedEvent(
        ExtendedDataEvent(
          auditSource = "merchandise-in-baggage-frontend",
          auditType = eventType,
          detail = toJson(declaration)
        )
      )
      .recover { case NonFatal(e) =>
        Failure(e.getMessage)
      }
      .map { status =>
        status match {
          case Success             =>
            logger.info(
              s"[Auditor][auditDeclaration] Successful audit of declaration with id [${declaration.declarationId}]"
            )
          case Disabled            =>
            logger.warn(
              s"[Auditor][auditDeclaration] Audit of declaration with id [${declaration.declarationId}] returned Disabled"
            )
          case Failure(message, _) =>
            logger.error(
              s"[Auditor][auditDeclaration] Audit of declaration with id [${declaration.declarationId}] returned Failure with message [$message]"
            )
        }
        ()
      }
  }

}
