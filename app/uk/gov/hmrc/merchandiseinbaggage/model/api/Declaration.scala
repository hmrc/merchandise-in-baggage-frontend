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

package uk.gov.hmrc.merchandiseinbaggage.model.api

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.merchandiseinbaggage.model.core._
import uk.gov.hmrc.merchandiseinbaggage.service.MibReferenceGenerator

case class Declaration(sessionId: SessionId,
                       declarationType: DeclarationType,
                       goodsDestination: GoodsDestination,
                       declarationGoods: DeclarationGoods,
                       nameOfPersonCarryingTheGoods: Name,
                       email: Email,
                       maybeCustomsAgent: Option[CustomsAgent],
                       eori: Eori,
                       journeyDetails: JourneyDetails,
                       dateOfDeclaration: LocalDateTime = LocalDateTime.now,
                       mibReference: MibReference = Declaration.mibReference
                      )

object Declaration extends MibReferenceGenerator {
  implicit val format: OFormat[Declaration] = Json.format[Declaration]
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM YYYY, h:mm a", Locale.ENGLISH)

  implicit class DeclarationDateTime(dateOfDeclaration: LocalDateTime) {
    def formattedDate: String = {
      dateOfDeclaration.format(formatter)
    }
  }
}
