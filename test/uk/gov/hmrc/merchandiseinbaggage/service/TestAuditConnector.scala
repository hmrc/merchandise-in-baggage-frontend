/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.JsValue
import uk.gov.hmrc.audit.HandlerResult

import scala.concurrent.{ExecutionContext, Future}
import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.inject.{ApplicationLifecycle, DefaultApplicationLifecycle}
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.{AuditChannel, AuditConnector, DatastreamMetrics}

abstract class TestAuditConnector(appName: String) extends AuditConnector {
  private val _auditingConfig =
    AuditingConfig(consumer = None, enabled = true, auditSource = "", auditSentHeaders = false)

  override def auditingConfig: AuditingConfig = _auditingConfig

  override def auditChannel: AuditChannel = new AuditChannel {
    override def auditingConfig: AuditingConfig = _auditingConfig

    override def materializer: Materializer = Materializer(ActorSystem())

    override def lifecycle: ApplicationLifecycle = new DefaultApplicationLifecycle()

    override def send(path: String, event: JsValue)(implicit ec: ExecutionContext): Future[HandlerResult] = {
      sendResult(path, event)
      Future.successful(HandlerResult.Success)
    }

    override def datastreamMetrics: DatastreamMetrics = DatastreamMetrics.disabled
  }

  override def datastreamMetrics: DatastreamMetrics = DatastreamMetrics.disabled

  def sendResult(path: String, event: JsValue): Future[HandlerResult]

}
