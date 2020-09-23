/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend

import java.time.LocalDate.now

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.{AmountInPence, MerchandiseDetails, MibReference, PayApitRequest, TraderDetails}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.{Address, CurrencyAmount, DeclarationJourney, Eori, GoodsEntry, JourneyDetails, Name, PriceOfGoods, SessionId}

trait CoreTestData {

  val payApiRequest: PayApitRequest = PayApitRequest(
    MibReference("MIBI1234567890"),
    AmountInPence(1),
    AmountInPence(2),
    AmountInPence(3),
    TraderDetails("Trader Inc, 239 Old Street, Berlin, Germany, EC1V 9EY"),
    MerchandiseDetails("Parts and technical crew for the forest moon")
  )

  val sessionId: SessionId = SessionId()

  val startedDeclarationJourney: DeclarationJourney = DeclarationJourney(sessionId)

  val completedDeclarationJourney: DeclarationJourney =
    DeclarationJourney(
      sessionId = sessionId,
      goodsEntries = Seq(
        GoodsEntry(
          "wine",
          Some("France"),
          Some(PriceOfGoods(CurrencyAmount(BigDecimal(100.00)), "EUR")),
          Some(CurrencyAmount(BigDecimal(10.00)))),
        GoodsEntry(
          "cheese",
          Some("France"),
          Some(PriceOfGoods(CurrencyAmount(BigDecimal(200.00)), "EUR")),
          Some(CurrencyAmount(BigDecimal(20.00))))),
      maybeName = Some(Name("Terry", "Test")),
      maybeAddress = Some(Address("1 Terry Terrace", "Terry Town", "T11 11T")),
      maybeEori = Some(Eori("TerrysEori")),
      maybeJourneyDetails = Some(JourneyDetails("Dover", now()))
    )
}
