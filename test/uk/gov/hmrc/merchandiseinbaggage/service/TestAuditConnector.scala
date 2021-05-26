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

import akka.stream.Materializer
import play.api.inject.{ApplicationLifecycle, Injector}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.{ExecutionContext, Future}

trait TestAuditConnector extends AuditConnector {
  def audited: Option[ExtendedDataEvent]
}

object TestAuditConnector {
  def apply(result: Future[AuditResult], injector: Injector): TestAuditConnector = new TestAuditConnector {
    override val auditingConfig: AuditingConfig = injector.instanceOf[AuditingConfig]
    override val materializer: Materializer = injector.instanceOf[Materializer]
    override val lifecycle: ApplicationLifecycle = injector.instanceOf[ApplicationLifecycle]

    private var auditedEvent: Option[ExtendedDataEvent] = None

    override def audited: Option[ExtendedDataEvent] = auditedEvent

    override def sendExtendedEvent(event: ExtendedDataEvent)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
      auditedEvent = Some(event)
      result
    }
  }
}
