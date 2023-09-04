/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.MockitoSugar.mock
import org.scalatest.concurrent.ScalaFutures
import play.api.i18n.MessagesApi
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.audit.HandlerResult
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.model.api.Declaration
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditorSpec extends BaseSpec with CoreTestData with ScalaFutures {

  private val failed: Failure           = Failure("failed")
  private val aMessagesApi: MessagesApi = mock[MessagesApi]

  private implicit val aHeaderCarrier: HeaderCarrier = HeaderCarrier()

  "auditDeclaration" should {
    Seq(Success, Disabled, failed).foreach { auditStatus =>
      s"delegate to the auditConnector and return $auditStatus" in {

        val testAuditConnector = new TestAuditConnector("TestApp") {
          override def sendResult(path: String, event: JsValue): Future[HandlerResult] = {
            (event \ "auditSource").get.toString mustBe "\"merchandise-in-baggage-frontend\""
            (event \ "auditType").get.toString mustBe "\"DeclarationPaymentAttempted\""
            (event \ "detail").get mustBe toJson(declaration)
            Future.successful(HandlerResult.Success)
          }
        }

        val auditService = new Auditor {
          override val auditConnector: AuditConnector = testAuditConnector
          override val messagesApi: MessagesApi       = aMessagesApi
        }

        auditService.auditDeclaration(declaration).futureValue mustBe a[Unit]
      }
    }

    "use DeclarationPaymentAttempted event for amendments" in {
      val aDeclarationWithAmendment: Declaration = declaration.copy(amendments = List(aAmendment))

      val testAuditConnector: TestAuditConnector = new TestAuditConnector("TestApp") {
        override def sendResult(path: String, event: JsValue): Future[HandlerResult] = {
          (event \ "auditSource").get.toString mustBe "\"merchandise-in-baggage-frontend\""
          (event \ "auditType").get.toString mustBe "\"DeclarationPaymentAttempted\""
          (event \ "detail").get mustBe toJson(aDeclarationWithAmendment)
          Future.successful(HandlerResult.Success)
        }
      }

      val auditService: Auditor = new Auditor {
        override val auditConnector: AuditConnector = testAuditConnector
        override val messagesApi: MessagesApi       = aMessagesApi
      }

      auditService.auditDeclaration(aDeclarationWithAmendment).futureValue mustBe a[Unit]
    }

    "handle auditConnector failure" in {
      val aDeclarationWithAmendment: Declaration = declaration.copy(amendments = List(aAmendment))

      val testAuditConnector: TestAuditConnector = new TestAuditConnector("TestApp") {
        override def sendResult(path: String, event: JsValue): Future[HandlerResult] =
          Future.successful(HandlerResult.Failure)
      }

      val auditService: Auditor = new Auditor {
        override val auditConnector: AuditConnector = testAuditConnector
        override val messagesApi: MessagesApi       = aMessagesApi
      }

      auditService.auditDeclaration(aDeclarationWithAmendment).futureValue mustBe a[Unit]
    }
  }
}
