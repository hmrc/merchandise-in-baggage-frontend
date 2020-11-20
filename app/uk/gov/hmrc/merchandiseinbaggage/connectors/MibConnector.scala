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

package uk.gov.hmrc.merchandiseinbaggage.connectors

import javax.inject.{Inject, Named, Singleton}
import uk.gov.hmrc.http.HttpReads.Implicits.readFromJson
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.merchandiseinbaggage.config.MibConfiguration
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationId

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MibConnector @Inject()(httpClient: HttpClient,  @Named("mibBackendBaseUrl") base: String) extends MibConfiguration {

  def persistDeclaration(declaration: Declaration)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[DeclarationId] =
    httpClient.POST[Declaration, DeclarationId](s"$base$declarationsUrl", declaration)

  def findDeclaration(declarationId: DeclarationId)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Declaration] =
    httpClient.GET[Declaration](s"$base$declarationsUrl/${declarationId.value}")
}
