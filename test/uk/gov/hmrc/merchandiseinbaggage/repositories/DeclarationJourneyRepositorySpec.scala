/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.repositories

import reactivemongo.bson.{BSONElement, BSONInteger}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}

class DeclarationJourneyRepositorySpec extends BaseSpecWithApplication with CoreTestData {
  override def beforeEach(): Unit = declarationJourneyRepository.deleteAll().futureValue

  "DeclarationJourneyRepository" should {
    "persist and find a declaration journey" in {
      val inserted = declarationJourneyRepository.insert(completedDeclarationJourney).futureValue

      inserted mustBe completedDeclarationJourney

      declarationJourneyRepository.findBySessionId(completedDeclarationJourney.sessionId).futureValue mustBe Some(
        completedDeclarationJourney)
    }

    "update a declaration journey" in {
      val update = completedDeclarationJourney.copy(maybeExciseOrRestrictedGoods = None)

      declarationJourneyRepository.insert(completedDeclarationJourney).futureValue
      declarationJourneyRepository.upsert(update).futureValue.n mustBe 1
      declarationJourneyRepository.findBySessionId(update.sessionId).futureValue mustBe Some(update)
    }

    "have a ttl index with the configured expiry time" in {
      val expectedTimeToLive = 3600

      declarationJourneyRepository.timeToLiveInSeconds mustBe expectedTimeToLive

      declarationJourneyRepository.indices.futureValue
        .filter(_.name.contains("timeToLive"))
        .head
        .options
        .elements
        .head mustBe BSONElement("expireAfterSeconds", BSONInteger(expectedTimeToLive))
    }
  }
}
