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

import org.scalatest.concurrent.ScalaFutures
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationId, MibReference, SessionId}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditorSpec extends BaseSpecWithApplication with CoreTestData with ScalaFutures {
  private val failed = Failure("failed")

  "auditDeclaration" should {
    Seq(Success, Disabled, failed).foreach { auditStatus =>
      s"delegate to the auditConnector and return $auditStatus" in new Auditor {
        override val auditConnector: TestAuditConnector = TestAuditConnector(Future.successful(auditStatus), injector)
        override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

        auditDeclaration(declaration).futureValue mustBe (())

        private val auditedEvent = auditConnector.audited.get
        auditedEvent.auditSource mustBe "merchandise-in-baggage-frontend"
        auditedEvent.auditType mustBe "DeclarationPaymentAttempted"
        auditedEvent.detail mustBe toJson(declaration)

        (auditedEvent.detail \ "source").as[String] mustBe "Digital"
      }
    }

    "use DeclarationPaymentAttempted event for amendments" in new Auditor {
      val aDeclarationWithAmendment = declaration.copy(amendments = List(aAmendment))

      override val auditConnector: TestAuditConnector = TestAuditConnector(Future.successful(Success), injector)
      override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      auditDeclaration(aDeclarationWithAmendment).futureValue mustBe (())

      private val auditedEvent = auditConnector.audited.get

      auditedEvent.auditType mustBe "DeclarationPaymentAttempted"
    }

    "handle auditConnector failure" in new Auditor {
      val aDeclarationWithAmendment = declaration.copy(amendments = List(aAmendment))

      override val auditConnector: TestAuditConnector =
        TestAuditConnector(Future.failed(new RuntimeException("failed")), injector)

      override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      auditDeclaration(aDeclarationWithAmendment).futureValue mustBe (())
    }
  }

  "DeclarationComplete" should {
    s"trigger new declarations" in new Auditor {
      private val aDeclaration =
        declaration.copy(
          declarationId = DeclarationId("123"),
          sessionId = SessionId("987"),
          mibReference = MibReference("MIB-A"),
          dateOfDeclaration = LocalDateTime.of(2021, 1, 1, 0, 0, 0, 0))

      override val auditConnector: TestAuditConnector = TestAuditConnector(Future.successful(Success), injector)
      override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      auditDeclaration(aDeclaration).futureValue mustBe (())

      private val auditedEvent = auditConnector.audited.get

      auditedEvent.auditSource mustBe "merchandise-in-baggage-frontend"
      auditedEvent.auditType mustBe "DeclarationPaymentAttempted"

      val completeDeclararionJson =
        s"""{"declarationId":"123","sessionId":"987","declarationType":"Import","goodsDestination":"GreatBritain","declarationGoods":{"goods":[{"category":"wine","goodsVatRate":"Twenty","producedInEu":"Yes","purchaseDetails":{"amount":"99.99","currency":{"code":"EUR","displayName":"title.euro_eur","valueForConversion":"EUR","currencySynonyms":["Europe","European"]}}},{"category":"cheese","goodsVatRate":"Twenty","producedInEu":"Yes","purchaseDetails":{"amount":"199.99","currency":{"code":"EUR","displayName":"title.euro_eur","valueForConversion":"EUR","currencySynonyms":["Europe","European"]}}}]},"nameOfPersonCarryingTheGoods":{"firstName":"Terry","lastName":"Test"},"email":{"email":"aa@test.com"},"maybeCustomsAgent":{"name":"Andy Agent","address":{"lines":["1 Agent Drive","Agent Town"],"postcode":"AG1 5NT","country":{"code":"GB","name":"United Kingdom"}}},"eori":{"value":"GB123467800000"},"journeyDetails":{"port":{"code":"DVR","displayName":"title.dover","isGB":true,"portSynonyms":["Port of Dover"]},"dateOfTravel":"${LocalDate.now.toString}","registrationNumber":"T5 RRY"},"dateOfDeclaration":"2021-01-01T00:00:00","mibReference":"MIB-A","emailsSent":false,"lang":"en","source":"Digital","amendments":[]}"""

      auditedEvent.detail mustBe Json.parse(completeDeclararionJson)
    }

    s"trigger Amendment declarations" in new Auditor {
      private val aDeclaration =
        declaration.copy(
          declarationId = DeclarationId("123"),
          sessionId = SessionId("987"),
          mibReference = MibReference("MIB-A"),
          maybeTotalCalculationResult = None,
          dateOfDeclaration = LocalDateTime.of(2021, 1, 1, 0, 0, 0, 0),
          amendments = List(aAmendmentPaid.copy(dateOfAmendment = LocalDateTime.of(2021, 1, 1, 0, 0, 0, 0)))
        )

      override val auditConnector: TestAuditConnector = TestAuditConnector(Future.successful(Success), injector)
      override val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

      auditDeclaration(aDeclaration).futureValue mustBe (())

      private val auditedEvent = auditConnector.audited.get

      auditedEvent.auditSource mustBe "merchandise-in-baggage-frontend"
      auditedEvent.auditType mustBe "DeclarationPaymentAttempted"

      val completeDeclararionJson =
        s"""{"declarationId":"123","sessionId":"987","declarationType":"Import","goodsDestination":"GreatBritain","declarationGoods":{"goods":[{"category":"wine","goodsVatRate":"Twenty","producedInEu":"Yes","purchaseDetails":{"amount":"99.99","currency":{"code":"EUR","displayName":"title.euro_eur","valueForConversion":"EUR","currencySynonyms":["Europe","European"]}}},{"category":"cheese","goodsVatRate":"Twenty","producedInEu":"Yes","purchaseDetails":{"amount":"199.99","currency":{"code":"EUR","displayName":"title.euro_eur","valueForConversion":"EUR","currencySynonyms":["Europe","European"]}}}]},"nameOfPersonCarryingTheGoods":{"firstName":"Terry","lastName":"Test"},"email":{"email":"aa@test.com"},"maybeCustomsAgent":{"name":"Andy Agent","address":{"lines":["1 Agent Drive","Agent Town"],"postcode":"AG1 5NT","country":{"code":"GB","name":"United Kingdom"}}},"eori":{"value":"GB123467800000"},"journeyDetails":{"port":{"code":"DVR","displayName":"title.dover","isGB":true,"portSynonyms":["Port of Dover"]},"dateOfTravel":"${LocalDate.now.toString}","registrationNumber":"T5 RRY"},"dateOfDeclaration":"2021-01-01T00:00:00","mibReference":"MIB-A","emailsSent":false,"lang":"en","source":"Digital","amendments":[{"reference":1,"dateOfAmendment":"2021-01-01T00:00:00","goods":{"goods":[{"category":"more cheese","goodsVatRate":"Twenty","producedInEu":"Yes","purchaseDetails":{"amount":"199.99","currency":{"code":"EUR","displayName":"title.euro_eur","valueForConversion":"EUR","currencySynonyms":["Europe","European"]}}}]},"maybeTotalCalculationResult":{"calculationResults":{"calculationResults":[{"goods":{"category":"wine","goodsVatRate":"Twenty","producedInEu":"Yes","purchaseDetails":{"amount":"99.99","currency":{"code":"EUR","displayName":"title.euro_eur","valueForConversion":"EUR","currencySynonyms":["Europe","European"]}}},"gbpAmount":10,"duty":5,"vat":7,"conversionRatePeriod":{"startDate":"2021-05-26","endDate":"2021-05-26","currencyCode":"EUR","rate":1.2}}]},"totalGbpValue":100,"totalTaxDue":100,"totalDutyDue":100,"totalVatDue":100},"paymentStatus":"Paid","source":"Digital","lang":"en","emailsSent":false}]}"""

      auditedEvent.detail mustBe Json.parse(completeDeclararionJson)
    }
  }
}
