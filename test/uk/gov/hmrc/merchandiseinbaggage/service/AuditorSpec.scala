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

package uk.gov.hmrc.merchandiseinbaggage.service

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpec, CoreTestData}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.{Disabled, Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditorSpec extends BaseSpec with CoreTestData with ScalaFutures {

  private val failed: Failure = Failure("failed")

  private implicit val aHeaderCarrier: HeaderCarrier = HeaderCarrier()

  "auditDeclaration" should {
    Seq(Success, Disabled, failed).foreach { auditStatus =>
      s"delegate to the auditConnector and return $auditStatus" in {

        val mockAuditConnector: AuditConnector = mock(classOf[AuditConnector])

        when(mockAuditConnector.sendExtendedEvent(any())(any(), any()))
          .thenReturn(Future.successful(auditStatus))

        val auditService = new Auditor {
          override val auditConnector: AuditConnector = mockAuditConnector
        }

        auditService.auditDeclaration(declaration).futureValue mustBe a[Unit]
      }
    }

  }
}
