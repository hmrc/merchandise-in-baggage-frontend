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

package uk.gov.hmrc.merchandiseinbaggage.scheduler

import akka.actor.{Actor, ActorLogging, Props}
import uk.gov.hmrc.merchandiseinbaggage.scheduler.SchedulingActor.ScheduledMessage
import uk.gov.hmrc.merchandiseinbaggage.service.DocumentUpdateService

class SchedulingActor extends Actor with ActorLogging {
  import context.dispatcher

  override def receive: Receive = { case message: ScheduledMessage[_] =>
    log.info(s"Received update request from the ${message.getClass.getSimpleName}")
    message.service.invoke
  }
}

object SchedulingActor {
  sealed trait ScheduledMessage[A] {
    val service: ScheduledService[A]
  }

  def props(): Props = Props[SchedulingActor]()

  case class UpdateDocumentsClass(service: DocumentUpdateService) extends ScheduledMessage[Boolean]
}
