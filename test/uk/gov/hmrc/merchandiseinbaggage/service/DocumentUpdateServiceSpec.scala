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

import org.bson.types.ObjectId
import org.mockito.ArgumentMatchers.any
import org.scalatest.{BeforeAndAfterEach, RecoverMethods}
import org.mockito.MockitoSugar
import play.api.mvc.ControllerComponents
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.merchandiseinbaggage.BaseSpec
import uk.gov.hmrc.merchandiseinbaggage.model._
import uk.gov.hmrc.merchandiseinbaggage.repositories.{DeclarationJourneyRepository, LockRepositoryProvider}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.Await.result
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class DocumentUpdateServiceSpec extends BaseSpec with BeforeAndAfterEach with RecoverMethods with MockitoSugar {

  val mockDeclarationJourneyMongo: DeclarationJourneyRepository = mock[DeclarationJourneyRepository]
  val mockLockKeeper: LockRepositoryProvider                    = mock[LockRepositoryProvider]
  val mockServicesConfig: ServicesConfig                        = mock[ServicesConfig]

  val mockCc: ControllerComponents  = stubControllerComponents()
  implicit val ec: ExecutionContext = mockCc.executionContext

  class Setup() {
    val documentUpdateService: DefaultDocumentUpdateService = new DefaultDocumentUpdateService(
      mockDeclarationJourneyMongo,
      mockLockKeeper,
      mockServicesConfig
    )
  }

  override def beforeEach(): Unit = {
    reset(mockDeclarationJourneyMongo)
    reset(mockLockKeeper)
    reset(mockServicesConfig)
  }

  "updateMissingCreatedAtFields" should {
    "run the update document scheduler job and return successful update message with modified count != 0" when {
      "there are documents without createdAt fields" in new Setup {
        when(mockDeclarationJourneyMongo.findCreatedAtString(any()))
          .thenReturn(Future.successful(Seq(ObjectId.get(), ObjectId.get())))
        when(mockDeclarationJourneyMongo.updateDate(any()))
          .thenReturn(Future.successful(2L))

        val updateMessage: RepositoryUpdateMessage =
          result(documentUpdateService.updateMissingCreatedAtFields(), Duration.Inf)

        updateMessage mustBe UpdateRequestAcknowledged(2)

        verify(mockDeclarationJourneyMongo, times(1)).findCreatedAtString(any[Int])
        verify(mockDeclarationJourneyMongo, times(1)).updateDate(any[Seq[ObjectId]])
      }
    }

    "run the update document scheduler job and return nothing to update message" when {
      "there are no documents without createdAt fields in the collection" in new Setup {
        when(mockDeclarationJourneyMongo.findCreatedAtString(any()))
          .thenReturn(Future.successful(Seq()))

        val updateMessage: RepositoryUpdateMessage =
          result(documentUpdateService.updateMissingCreatedAtFields(), Duration.Inf)

        updateMessage mustBe UpdateRequestNothingToUpdate

        verify(mockDeclarationJourneyMongo, times(1)).findCreatedAtString(any[Int])
        verify(mockDeclarationJourneyMongo, times(0)).updateDate(any[Seq[ObjectId]])
      }
    }

    "run the update document scheduler job and return request not acknowledged message" when {
      "nothing was updated in the database but documents without created at field exist" in new Setup {
        when(mockDeclarationJourneyMongo.findCreatedAtString(any()))
          .thenReturn(Future.successful(Seq(ObjectId.get())))
        when(mockDeclarationJourneyMongo.updateDate(any()))
          .thenReturn(Future.successful(0L))

        val updateMessage: RepositoryUpdateMessage =
          result(documentUpdateService.updateMissingCreatedAtFields(), Duration.Inf)

        updateMessage mustBe UpdateRequestNotAcknowledged

        verify(mockDeclarationJourneyMongo, times(1)).findCreatedAtString(any[Int])
        verify(mockDeclarationJourneyMongo, times(1)).updateDate(any[Seq[ObjectId]])
      }
    }

    "propagate database exception to the caller" in new Setup {
      when(mockDeclarationJourneyMongo.findCreatedAtString(any()))
        .thenReturn(Future.successful(Seq(ObjectId.get())))
      when(mockDeclarationJourneyMongo.updateDate(any()))
        .thenReturn(Future.failed(new RuntimeException))

      recoverToSucceededIf[RuntimeException] {
        documentUpdateService.updateMissingCreatedAtFields()
      }
    }
  }
}
