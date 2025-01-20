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

package uk.gov.hmrc.merchandiseinbaggage.utils

import play.api.Logger
import play.api.libs.json.Json.{prettyPrint, toJson}
import play.api.mvc.{Request, RequestHeader}
import uk.gov.hmrc.http.CookieNames.deviceID
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.controllers.DeclarationJourneyRequest
import uk.gov.hmrc.merchandiseinbaggage.utils.Obfuscate._
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider

object DeclarationJourneyLogger {
  private val logger: Logger = Logger("DeclarationJourneyLogger")

  def info(message: => String)(implicit request: RequestHeader): Unit = logMessage(message, Info)

  def warn(message: => String)(implicit request: RequestHeader): Unit = logMessage(message, Warn)

  private sealed trait LogLevel

  private case object Info extends LogLevel

  private case object Warn extends LogLevel

  private def logMessage(message: => String, level: LogLevel)(implicit request: RequestHeader): Unit = {
    lazy val richMessage = makeRichMessage(message)
    level match {
      case Info => logger.info(richMessage)
      case Warn => logger.warn(richMessage)
    }
  }

  private def headerCarrier(implicit request: Request[?]): HeaderCarrier = HcProvider.headerCarrier

  private def sessionId(implicit r: RequestHeader): String = {
    val hc = r match {
      case r: Request[_] => headerCarrier(r)
      case r             => HeaderCarrierConverter.fromRequest(r)
    }
    s"sessionId: [${hc.sessionId.map(_.value).getOrElse("")}]"
  }

  private def deviceId(implicit r: RequestHeader) =
    s"deviceId: [${r.cookies.find(_.name == deviceID).map(_.value).getOrElse("")}]"

  private def context(implicit r: RequestHeader) = s"context: [${r.method} ${r.path}]] $sessionId $deviceId"

  private def obfuscatedDeclarationJourney(declarationJourneyRequest: DeclarationJourneyRequest[?]) =
    s"declarationJourney: [${prettyPrint(toJson(declarationJourneyRequest.declarationJourney.obfuscated))}]"

  private def makeRichMessage(message: String)(implicit request: RequestHeader): String = request match {
    case declarationJourneyRequest: DeclarationJourneyRequest[_] =>
      s"$message ${obfuscatedDeclarationJourney(declarationJourneyRequest)} $context"
    case _                                                       =>
      s"$message declarationJourney: [] $context"
  }
}

private object HcProvider extends FrontendHeaderCarrierProvider {
  def headerCarrier(implicit request: Request[?]): HeaderCarrier = hc(request)
}
