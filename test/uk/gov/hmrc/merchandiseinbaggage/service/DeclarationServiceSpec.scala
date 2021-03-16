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

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Amendment, AmountInPence, DeclarationId, Paid}
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub.givenPersistedDeclarationIsFound
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.merchandiseinbaggage.wiremock.WireMockSupport

import java.time.LocalDate

class DeclarationServiceSpec extends BaseSpecWithApplication with WireMockSupport with CoreTestData {

  "Declaration time" should {
    "more than 5 days before travel" in {
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 2, 1)

      val result = DeclarationService.withinTimerange(travelDate, now)
      result mustBe false
    }

    "within 5 days of travel" in {
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 2, 9)

      val result = DeclarationService.withinTimerange(travelDate, now)
      result mustBe true
    }

    "within 30 days to allow update" in {
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 2, 11)

      val result = DeclarationService.withinTimerange(travelDate, now)
      result mustBe true
    }

    "greated than 30 days after travel" in {
      val travelDate = LocalDate.of(2021, 2, 10)
      val now = LocalDate.of(2021, 3, 15)

      val result = DeclarationService.withinTimerange(travelDate, now)
      result mustBe false
    }
  }

  "list of goods" should {
    "only contain goods when no amendments" in {
      val goods = List.fill(10)(aGoods)
      val result = DeclarationService.listGoods(goods, List.empty[Amendment])

      result.length mustBe 10
    }

    "only contain amendment that have been Paid/NotRequired" in {
      val goods = List.fill(10)(aGoods)

      val amendments: Seq[Amendment] = List(aAmendmentPaid)
      val result = DeclarationService.listGoods(goods, amendments)

      result.length mustBe 11
    }

    "only contain amendments that have been Paid or NotRequired and not unpaid" in {
      val goods = List.fill(10)(aGoods)

      val amendments = List(
        aAmendmentPaid,
        aAmendment,
        aAmendmentNotRequired
      )
      val result = DeclarationService.listGoods(goods, amendments)

      result.length mustBe 12
    }

    "contain all the goods with amendments at the end" in {
      val itemCount = 10
      val goods = List.fill(itemCount)(aGoods)
      val amendments = List(aAmendmentPaid)
      val result = DeclarationService.listGoods(goods, amendments)

      result.length mustBe itemCount + 1
      result.last.categoryQuantityOfGoods.category mustBe "more cheese"
    }
  }

  "total payment" should {
    "match declaration if no amendments" in {
      val dec = AmountInPence(1000L)
      val result = DeclarationService.totalPayment(dec, List.empty[Amendment])

      result.value mustBe 1000L
    }

    "calc payment with declaration and one amendments" in {
      val dec = AmountInPence(1000L)
      val amendments = List(aAmendmentPaid)
      val result = DeclarationService.totalPayment(dec, amendments)

      result.value mustBe 1100L
    }

    "calc payment with declaration and several amendments" in {
      val dec = AmountInPence(1000L)
      val itemCount = 10
      val amendment = List.fill(itemCount)(aAmendmentPaid)

      val result = DeclarationService.totalPayment(dec, amendment)

      result.value mustBe 2000L
    }
  }

  "the mib backend " should {
    "retrieve data when pressent" in {
      implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val service = injector.instanceOf[DeclarationService]

      val declarationId = DeclarationId("123")

      val importDeclaration = declaration
        .copy(maybeTotalCalculationResult = Some(aTotalCalculationResult), paymentStatus = Some(Paid), amendments = Seq(aAmendmentPaid))

      givenPersistedDeclarationIsFound(importDeclaration, declarationId)

      service.findDeclaration(declarationId)(hc, ec).futureValue.map {
        case (goods, journeyDetails, declarationType, withinDate, totalPayments) =>
          goods.length mustBe (3)
          journeyDetails mustBe (declaration.journeyDetails)
          declarationType mustBe (Import)
          withinDate mustBe (true)
          totalPayments.value mustBe (200L)
        case err => fail(err.toString())
      }
    }

    "retrieve nothing when invalid declarationId" in {
      implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val service = injector.instanceOf[DeclarationService]

      val declarationId = DeclarationId("123")
      givenPersistedDeclarationIsFound(declaration, declarationId)

      service.findDeclaration(DeclarationId("987"))(hc, ec).futureValue.map {
        case (_, _, _, _, _) =>
          fail("Should not be able to find Declaration")
      }
    }
  }
}
