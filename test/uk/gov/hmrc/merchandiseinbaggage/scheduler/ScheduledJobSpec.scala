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

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.extension.quartz.QuartzSchedulerExtension
import org.quartz.CronExpression
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.merchandiseinbaggage.scheduler.SchedulingActor.UpdateDocumentsClass
import uk.gov.hmrc.merchandiseinbaggage.service.DocumentUpdateService
import org.mockito.Mockito.{mock, reset}

class ScheduledJobSpec extends AnyWordSpecLike with Matchers {
  val jobNameTest                                            = "testJobName"
  val mockActorSystem: ActorSystem                           = mock(classOf[ActorSystem])
  val mockService: DocumentUpdateService                     = mock(classOf[DocumentUpdateService])
  val mockApplicationLifecycle: ApplicationLifecycle         = mock(classOf[ApplicationLifecycle])
  val mockQuartzSchedulerExtension: QuartzSchedulerExtension = mock(classOf[QuartzSchedulerExtension])

  class Setup(cronString: String, enabled: Boolean = false) {
    val testConfig: Configuration = Configuration(
      s"schedules.$jobNameTest.expression"  -> s"$cronString",
      s"schedules.$jobNameTest.enabled"     -> enabled,
      s"schedules.$jobNameTest.description" -> "testDescription"
    )

    reset(mockQuartzSchedulerExtension)
    reset(mockService)
    reset(mockActorSystem)

    val job: ScheduledJob = new ScheduledJob {
      val scheduledMessage: SchedulingActor.ScheduledMessage[_] = UpdateDocumentsClass(mockService)
      override val config: Configuration                        = testConfig
      val actorSystem: ActorSystem                              = mockActorSystem
      override lazy val jobName: String                         = jobNameTest
      override lazy val scheduler: QuartzSchedulerExtension     = mockQuartzSchedulerExtension
    }
  }

  "expression should read from string correctly with underscores" in new Setup("0_*/10_0-23_?_*_*_*") {
    job.expression shouldBe "0 */10 0-23 ? * * *"
  }

  "isValid should return true if valid cron config returned" in new Setup("0_*/2_0-23_?_*_*_*") {
    job.isValid shouldBe true
  }

  "isValid should return false if an invalid cron config is returned" in new Setup("testInvalidCronString") {
    job.isValid shouldBe false
  }

  "isValid should return false if empty string is returned" in new Setup("") {
    job.isValid shouldBe false
  }

  // run job every 10 seconds every hour
  "expression once converted should convert to a cron expression success" in new Setup("*/10_0_0-23_?_*_*_*") {
    val parsed = new CronExpression(job.expression)
    parsed.getCronExpression    shouldBe "*/10 0 0-23 ? * * *"
    parsed.getExpressionSummary shouldBe
      """seconds: 0,10,20,30,40,50
        |minutes: 0
        |hours: 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23
        |daysOfMonth: ?
        |months: *
        |daysOfWeek: *
        |lastdayOfWeek: false
        |nearestWeekday: false
        |NthDayOfWeek: 0
        |lastdayOfMonth: false
        |years: *
        |""".stripMargin
  }

  // run job every 5 minutes between 8-18
  "expression for local dev once converted should convert to a cron expression success" in new Setup(
    "0_*/5_8-18_*_*_?"
  ) {
    val parsed = new CronExpression(job.expression)
    parsed.getCronExpression    shouldBe "0 */5 8-18 * * ?"
    parsed.getExpressionSummary shouldBe
      """seconds: 0
        |minutes: 0,5,10,15,20,25,30,35,40,45,50,55
        |hours: 8,9,10,11,12,13,14,15,16,17,18
        |daysOfMonth: *
        |months: *
        |daysOfWeek: ?
        |lastdayOfWeek: false
        |nearestWeekday: false
        |NthDayOfWeek: 0
        |lastdayOfMonth: false
        |years: *
        |""".stripMargin
  }

  // run job every 5 minutes between 8-18
  "expression for QA once converted should convert to a cron expression success" in new Setup("0_*/5_8-18_*_*_?") {
    val parsed = new CronExpression(job.expression)
    parsed.getCronExpression    shouldBe "0 */5 8-18 * * ?"
    parsed.getExpressionSummary shouldBe
      """seconds: 0
        |minutes: 0,5,10,15,20,25,30,35,40,45,50,55
        |hours: 8,9,10,11,12,13,14,15,16,17,18
        |daysOfMonth: *
        |months: *
        |daysOfWeek: ?
        |lastdayOfWeek: false
        |nearestWeekday: false
        |NthDayOfWeek: 0
        |lastdayOfMonth: false
        |years: *
        |""".stripMargin
  }

  // run job every 10 minutes between 18-8
  "expression for Staging/Prod once converted should convert to a cron expression success" in new Setup(
    "0_*/10_18-23,0-8_*_*_?"
  ) {
    val parsed = new CronExpression(job.expression)
    parsed.getCronExpression    shouldBe "0 */10 18-23,0-8 * * ?"
    parsed.getExpressionSummary shouldBe
      """seconds: 0
        |minutes: 0,10,20,30,40,50
        |hours: 0,1,2,3,4,5,6,7,8,18,19,20,21,22,23
        |daysOfMonth: *
        |months: *
        |daysOfWeek: ?
        |lastdayOfWeek: false
        |nearestWeekday: false
        |NthDayOfWeek: 0
        |lastdayOfMonth: false
        |years: *
        |""".stripMargin
  }

  "scheduler called if enabled and valid cron config" in new Setup("*/10_0_0-23_?_*_*_*", enabled = true) {
    job.schedule shouldBe true
  }

  "scheduler NOT called if not enabled and cron config invalid" in new Setup("testInvalidCronString", enabled = false) {
    job.schedule shouldBe false
  }

  "scheduler NOT called if enabled and cron config invalid" in new Setup("testInvalidCronString", enabled = true) {
    job.schedule shouldBe false
  }
}
