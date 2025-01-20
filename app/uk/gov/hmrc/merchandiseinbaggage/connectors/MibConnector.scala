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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import cats.data.EitherT
import play.api.Logging
import play.api.http.Status
import play.api.libs.json.Json
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.{CalculationAmendRequest, CalculationRequest, CalculationResponse}
import uk.gov.hmrc.merchandiseinbaggage.model.api.checkeori.CheckResponse
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Declaration, DeclarationId, Eori, MibReference}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MibConnector @Inject() (appConfig: AppConfig, httpClient: HttpClientV2)(implicit
  ec: ExecutionContext
) extends Logging {

  private val baseUrl                                           = appConfig.merchandiseInBaggageUrl
  private val declarationsUrl                                   = s"$baseUrl${appConfig.mibDeclarationsUrl}"
  private def oneDeclarationUrl(declarationId: DeclarationId)   =
    s"$baseUrl${appConfig.mibDeclarationsUrl}/${declarationId.value}"
  private val calculationsUrl                                   = s"$baseUrl${appConfig.mibCalculationsUrl}"
  private val amendCalculationsUrl                              = s"$baseUrl${appConfig.mibAmendsPlusExistingCalculationsUrl}"
  private def findByUrl(mibReference: MibReference, eori: Eori) =
    s"$baseUrl${appConfig.mibDeclarationsUrl}?mibReference=${mibReference.value}&eori=${eori.value}"
  private def eoriUrl(eori: String)                             = s"$baseUrl${appConfig.mibCheckEoriUrl}$eori"

  def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier): Future[DeclarationId] =
    httpClient
      .post(url"$declarationsUrl")
      .withBody(Json.toJson(declaration))
      .execute[DeclarationId]

  def amendDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier): Future[DeclarationId] =
    httpClient
      .put(url"$declarationsUrl")
      .withBody(Json.toJson(declaration))
      .execute[DeclarationId]

  def findDeclaration(declarationId: DeclarationId)(implicit hc: HeaderCarrier): Future[Option[Declaration]] =
    httpClient
      .get(url"${oneDeclarationUrl(declarationId)}")
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case Status.OK => response.json.asOpt[Declaration]
          case other     =>
            logger.warn(s"[MibConnector][findDeclaration] unexpected status for findDeclaration, status:$other")
            None
        }
      }

  def findBy(mibReference: MibReference, eori: Eori)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, String, Option[Declaration]] =
    EitherT(
      httpClient
        .get(url"${findByUrl(mibReference, eori)}")
        .execute[HttpResponse]
        .map { response =>
          response.status match {
            case Status.OK        => Right(response.json.asOpt[Declaration])
            case Status.NOT_FOUND => Right(None)
            case other            =>
              logger.warn(
                s"[MibConnector][findBy] unexpected status for findBy for mibReference:${mibReference.value}, and eori:${eori.value}, status:$other"
              )
              Left(s"unexpected status for findBy, status:$other")
          }
        }
    )

  def calculatePayments(calculationRequests: Seq[CalculationRequest])(implicit
    hc: HeaderCarrier
  ): Future[CalculationResponse] =
    httpClient
      .post(url"$calculationsUrl")
      .withBody(Json.toJson(calculationRequests))
      .execute[CalculationResponse]

  def calculatePaymentsAmendPlusExisting(
    amendRequest: CalculationAmendRequest
  )(implicit hc: HeaderCarrier): Future[CalculationResponse] =
    httpClient
      .post(url"$amendCalculationsUrl")
      .withBody(Json.toJson(amendRequest))
      .execute[CalculationResponse]

  def checkEoriNumber(eori: String)(implicit hc: HeaderCarrier): Future[CheckResponse] =
    httpClient
      .get(url"${eoriUrl(eori)}")
      .execute[CheckResponse]

}
