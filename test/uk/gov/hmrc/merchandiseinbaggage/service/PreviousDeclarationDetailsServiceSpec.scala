/*
 * Copyright 2021 HM Revenue & Customs
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

import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, CategoryQuantityOfGoods, DeclarationGoods, Paid, PaymentStatus}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import java.time.{LocalDate, LocalDateTime}

class PreviousDeclarationDetailsServiceSpec extends BaseSpecWithApplication with WireMockSupport with CoreTestData {

  def createAmendment(desc: String, paymentStatus: Option[PaymentStatus]): Amendment = Amendment(
    LocalDateTime.now,
    DeclarationGoods(aGoods.copy(categoryQuantityOfGoods = CategoryQuantityOfGoods(desc, "123")) :: Nil),
    None,
    paymentStatus,
    Some("Digital")
  )

  "Declaration time" should {
    "more than 5 days before travel" in {
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 2, 1)

      val result = PreviousDeclarationDetailsService.withinTimerange(travelDate, now)
      result mustBe false
    }

    "within 5 days of travel" in {
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 2, 9)

      val result = PreviousDeclarationDetailsService.withinTimerange(travelDate, now)
      result mustBe true
    }

    "within 30 days to allow update" in {
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 2, 11)

      val result = PreviousDeclarationDetailsService.withinTimerange(travelDate, now)
      result mustBe true
    }

    "greated than 30 days after travel" in {
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 3, 15)

      val result = PreviousDeclarationDetailsService.withinTimerange(travelDate, now)
      result mustBe false
    }
  }

  "list of goods" should {
    "only contain goods when no amendments" in {
      val goods = List.fill(10)(aGoods)
      val result = PreviousDeclarationDetailsService.listGoods(goods, List.empty[Amendment])

      result.length mustBe 10
    }

    "only contain amendment that have been Paid/NotRequired" in {
      val goods = List.fill(10)(aGoods)

      val amendments: Seq[Amendment] = List(createAmendment("amend", Some(Paid)))
      val result = PreviousDeclarationDetailsService.listGoods(goods, amendments)

      result.length mustBe 11
    }

    "only contain amendments that have been Paid or NotRequired and not unpaid" in {
      val goods = List.fill(10)(aGoods)

      val amendments = List(
        aAmendmentPaid,
        aAmendment,
        aAmendmentNotRequired
      )
      val result = PreviousDeclarationDetailsService.listGoods(goods, amendments)

      result.length mustBe 12
    }

    "contain all the goods with amendments at the end" in {
      val itemCount = 10
      val goods = List.fill(itemCount)(aGoods)
      val amendments = List(createAmendment("amend", Some(Paid)))
      val result = PreviousDeclarationDetailsService.listGoods(goods, amendments)

      result.length mustBe itemCount + 1
      result.last.categoryQuantityOfGoods.category mustBe "amend"
    }
  }

}
