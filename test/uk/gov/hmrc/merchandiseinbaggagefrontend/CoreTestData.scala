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

package uk.gov.hmrc.merchandiseinbaggagefrontend

import java.time.LocalDate.now

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.GoodsVatRate
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.currencyconversion.Currency
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration._

trait CoreTestData {

  val payApiRequest: PayApiRequest = PayApiRequest(
    MibReference("MIBI1234567890"),
    AmountInPence(1),
    AmountInPence(2),
    AmountInPence(3),
    TraderDetails("Trader Inc, 239 Old Street, Berlin, Germany, EC1V 9EY"),
    MerchandiseDetails("Parts and technical crew for the forest moon")
  )

  val sessionId: SessionId = SessionId()

  val startedDeclarationJourney: DeclarationJourney = DeclarationJourney(sessionId)

  val completedGoodsEntry: GoodsEntry =
    GoodsEntry(
      CategoryQuantityOfGoods("wine", "1"),
      Some(GoodsVatRate.Twenty),
      Some("France"),
      Some(PriceOfGoods(BigDecimal(99.99), Currency("Eurozone", "Euro", "EUR"))),
      Some("1234560"),
      Some(BigDecimal(10.11)))

  val completedDeclarationJourney: DeclarationJourney =
    DeclarationJourney(
      sessionId = sessionId,
      maybeExciseOrRestrictedGoods = Some(false),
      goodsEntries = Seq(
        completedGoodsEntry,
        GoodsEntry(
          CategoryQuantityOfGoods("cheese", "3"),
          Some(GoodsVatRate.Twenty),
          Some("France"),
          Some(PriceOfGoods(BigDecimal(199.99), Currency("Eurozone", "Euro", "EUR"))),
          Some("1234560"),
          Some(BigDecimal(20.22)))),
      maybeName = Some(Name("Terry", "Test")),
      maybeAddress = Some(Address("1 Terry Terrace", "Terry Town", "T11 11T")),
      maybeEori = Some(Eori("TerrysEori")),
      maybeJourneyDetails = Some(JourneyDetails("Dover", now()))
    )

  val declaration: Declaration = completedDeclarationJourney.toDeclarationIfComplete.get

  val incompleteDeclarationJourney: DeclarationJourney = completedDeclarationJourney.copy(maybeJourneyDetails = None)
}
