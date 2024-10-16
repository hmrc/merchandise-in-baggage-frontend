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

import com.google.inject.Inject
import play.api.Logging
import uk.gov.hmrc.merchandiseinbaggage.model._
import uk.gov.hmrc.merchandiseinbaggage.repositories.{DeclarationJourneyRepository, LockRepositoryProvider}
import uk.gov.hmrc.merchandiseinbaggage.scheduler.ScheduledService
import uk.gov.hmrc.mongo.lock.LockService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.duration.{Duration, SECONDS}
import scala.concurrent.{ExecutionContext, Future}

trait DocumentUpdateService extends ScheduledService[Boolean] with Logging
class DefaultDocumentUpdateService @Inject() (
  repository: DeclarationJourneyRepository,
  lockRepositoryProvider: LockRepositoryProvider,
  servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext)
    extends DocumentUpdateService {

  override val jobName: String         = "update-created-at-field-job"
  private val updateLimit: Int         = servicesConfig.getInt(s"schedules.$jobName.updateLimit")
  private lazy val lockoutTimeout: Int = servicesConfig.getInt(s"schedules.$jobName.lockTimeout")
  private val lockService: LockService =
    LockService(
      lockRepositoryProvider.repo,
      lockId = "update-created-at-job-lock",
      ttl = Duration.create(lockoutTimeout, SECONDS)
    )

  private[service] def updateMissingCreatedAtFields(): Future[RepositoryUpdateMessage] =
    repository.findCreatedAtString(updateLimit).flatMap { documentIds =>
      if (documentIds.nonEmpty) {
        repository.updateDate(documentIds).map {
          case updatedCount if updatedCount > 0 => UpdateRequestAcknowledged(updatedCount)
          case _                                => UpdateRequestNotAcknowledged
        }
      } else {
        Future.successful(UpdateRequestNothingToUpdate)
      }
    }

  override def invoke(implicit ec: ExecutionContext): Future[Boolean] =
    lockService.withLock(updateMissingCreatedAtFields()).map {
      case Some(updateMessage) =>
        logger.info(s"[DefaultDocumentUpdateService][invoke] ${updateMessage.message}")
        true
      case None                =>
        logger.info(s"[DefaultDocumentUpdateService][invoke] ${FailedToLockRepositoryForUpdate.message}")
        false
    }
}
