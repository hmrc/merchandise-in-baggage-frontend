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

package uk.gov.hmrc.merchandiseinbaggage.scheduler

import org.apache.pekko.actor.{ActorRef, ActorSystem}
import org.apache.pekko.extension.quartz.QuartzSchedulerExtension
import org.quartz.CronExpression
import play.api.{Configuration, Logging}
import uk.gov.hmrc.merchandiseinbaggage.scheduler.SchedulingActor.ScheduledMessage

import java.time.ZoneId
import java.util.TimeZone

trait ScheduledJob extends Logging {
  val scheduledMessage: ScheduledMessage[?]
  val config: Configuration
  val actorSystem: ActorSystem
  def jobName: String

  lazy val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(actorSystem)

  private lazy val schedulingActorRef: ActorRef = actorSystem.actorOf(SchedulingActor.props())

  private[scheduler] lazy val enabled: Boolean =
    config.getOptional[Boolean](s"schedules.$jobName.enabled").getOrElse(false)

  private lazy val description: Option[String] = config.getOptional[String](s"schedules.$jobName.description")

  private[scheduler] lazy val expression: String =
    config.getOptional[String](s"schedules.$jobName.expression") map (_.replaceAll("_", " ")) getOrElse ""

  private lazy val timezone: String =
    config.getOptional[String](s"schedules.$jobName.timezone").getOrElse(TimeZone.getDefault.getID)

  private[scheduler] lazy val isValid = expression.nonEmpty && CronExpression.isValidExpression(expression)

  lazy val schedule: Boolean =
    (enabled, isValid) match {
      case (true, true)  =>
        scheduler.createSchedule(jobName, description, expression, None, TimeZone.getTimeZone(ZoneId.of(timezone)))
        scheduler.schedule(jobName, schedulingActorRef, scheduledMessage)
        logger.info(s"[ScheduledJob][schedule] Scheduler for $jobName has been started")
        true
      case (true, false) =>
        logger.info(
          s"[ScheduledJob][schedule] Scheduler for $jobName is disabled as there is no quartz expression or expression is not valid"
        )
        false
      case (false, _)    =>
        logger.info(s"[ScheduledJob][schedule] Scheduler for $jobName is disabled by configuration")
        false
    }
}
