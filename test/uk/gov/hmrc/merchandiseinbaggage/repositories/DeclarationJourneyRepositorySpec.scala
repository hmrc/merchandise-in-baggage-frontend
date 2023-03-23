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

package uk.gov.hmrc.merchandiseinbaggage.repositories

import org.bson.types.ObjectId
import org.mongodb.scala.Document
import org.mongodb.scala.model.{Filters, Updates}
import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData}
import uk.gov.hmrc.mongo.play.json.Codecs

import java.time.LocalDateTime

class DeclarationJourneyRepositorySpec extends BaseSpecWithApplication with CoreTestData {

  private val declarationOtherJourney = completedDeclarationJourney.copy(sessionId = SessionId("c55ef3b3-db49-4cc8-956b-ab52546dee64"))

  "DeclarationJourneyRepository" should {

    "findAll finds zero matches when no declarations made" in {
      declarationJourneyRepository.collection.find[Document]().toFuture().futureValue.size mustBe 0
    }

    "persist and find a declaration journey" in {
      val inserted = declarationJourneyRepository.upsert(completedDeclarationJourney).futureValue

      inserted mustBe completedDeclarationJourney

      declarationJourneyRepository.findBySessionId(completedDeclarationJourney.sessionId).futureValue mustBe Some(
        completedDeclarationJourney)
    }

    "update a declaration journey" in {
      val update = completedDeclarationJourney.copy(maybeExciseOrRestrictedGoods = None)

      declarationJourneyRepository.upsert(completedDeclarationJourney).futureValue
      declarationJourneyRepository.upsert(update).futureValue mustBe update
      declarationJourneyRepository.findBySessionId(update.sessionId).futureValue mustBe Some(update)
    }

    "have a ttl index with the configured expiry time" in {
      val expectedTimeToLive = 3600

      declarationJourneyRepository.appConfig.mongoTTL mustBe expectedTimeToLive

      val indices = declarationJourneyRepository.collection.listIndexes().toFuture().futureValue

      val actualTimeToLive = indices.find(_.toString.contains("timeToLive")).get("expireAfterSeconds").asInt64().intValue()

      actualTimeToLive mustBe expectedTimeToLive
    }
  }

  "getSessionIdsWithStringCreatedAt" should {
    "return list of session ids with createdAt field of type string" in {

      val testDocumentId = declarationJourneyRepository.collection
        .insertOne(completedDeclarationJourney)
        .toFuture()
        .futureValue
        .getInsertedId
        .asObjectId()
        .getValue //document without createdAt
      val testDocumentId2 = declarationJourneyRepository.collection
        .insertOne(declarationOtherJourney)
        .toFuture()
        .futureValue
        .getInsertedId
        .asObjectId()
        .getValue

      val selector = Filters.equal("sessionId", Codecs.toBson(completedDeclarationJourney.sessionId))
      val selectorOther = Filters.equal("sessionId", Codecs.toBson(declarationOtherJourney.sessionId))
      val update = Updates.set("createdAt", "2023-03-21T15:05:20.747Z")
      declarationJourneyRepository.collection.findOneAndUpdate(selector, update).toFuture().futureValue
      declarationJourneyRepository.collection.findOneAndUpdate(selectorOther, update).toFuture().futureValue

      declarationJourneyRepository.findCreatedAtString(2).futureValue mustBe Seq(testDocumentId, testDocumentId2)
    }

    "return empty list of document ids if createdAt with type date exists for every record" in {
      declarationJourneyRepository.upsert(completedDeclarationJourney).futureValue //document with createdAt type date
      declarationJourneyRepository.upsert(declarationOtherJourney).futureValue //document with createdAt type date

      declarationJourneyRepository.findCreatedAtString(2).futureValue mustBe Seq.empty
    }
  }

  "addCreatedAtField" should {
    "add createdAt field to one document without this field" in {
      // Insert dummy data
      declarationJourneyRepository.upsert(completedDeclarationJourney).futureValue
      declarationJourneyRepository.upsert(declarationOtherJourney).futureValue

      // Set one set of data to use string CreatedAt
      val selector = Filters.equal("sessionId", Codecs.toBson(declarationOtherJourney.sessionId))
      val update = Updates.set("createdAt", "2020")
      declarationJourneyRepository.collection.findOneAndUpdate(selector, update).toFuture().futureValue

      // Find where createdAt is string and update
      val documentIds: Seq[ObjectId] = declarationJourneyRepository.findCreatedAtString(2).futureValue
      val updateTime = declarationJourneyRepository.updateDate(documentIds).futureValue

      // Checks collection sizes and returns are correct
      declarationJourneyRepository.collection.find(Filters.empty()).toFuture().futureValue.size mustBe 2
      documentIds.size mustBe 1
      updateTime mustBe 1

      // Checks it is the correct data type
      declarationJourneyRepository.collection
        .find(selector)
        .toFuture()
        .futureValue
        .head
        .createdAt mustBe a[LocalDateTime]
    }

    "add createdAt field to multiple documents without this field" in {
      // Insert dummy data
      declarationJourneyRepository.upsert(completedDeclarationJourney).futureValue
      declarationJourneyRepository.upsert(declarationOtherJourney).futureValue

      // Set both sets of data to use string CreatedAt
      val selector = Filters.equal("sessionId", Codecs.toBson(completedDeclarationJourney.sessionId))
      val selectorOther = Filters.equal("sessionId", Codecs.toBson(declarationOtherJourney.sessionId))
      val update = Updates.set("createdAt", "2020")
      declarationJourneyRepository.collection.findOneAndUpdate(selector, update).toFuture().futureValue
      declarationJourneyRepository.collection.findOneAndUpdate(selectorOther, update).toFuture().futureValue

      // Find where createdAt is string and update
      val documentIds: Seq[ObjectId] = declarationJourneyRepository.findCreatedAtString(2).futureValue
      val updateTime = declarationJourneyRepository.updateDate(documentIds).futureValue

      // Checks collection sizes and returns are correct
      declarationJourneyRepository.collection.find(Filters.empty()).toFuture().futureValue.size mustBe 2
      documentIds.size mustBe 2
      updateTime mustBe 2
    }

    "add createdAt field to same number of documents as updateLimit" in {
      // Insert dummy data
      declarationJourneyRepository.upsert(completedDeclarationJourney).futureValue
      declarationJourneyRepository.upsert(declarationOtherJourney).futureValue

      // Set both sets of data to use string CreatedAt
      val selector = Filters.equal("sessionId", Codecs.toBson(completedDeclarationJourney.sessionId))
      val selectorOther = Filters.equal("sessionId", Codecs.toBson(declarationOtherJourney.sessionId))
      val update = Updates.set("createdAt", "2020")
      declarationJourneyRepository.collection.findOneAndUpdate(selector, update).toFuture().futureValue
      declarationJourneyRepository.collection.findOneAndUpdate(selectorOther, update).toFuture().futureValue

      // Find where createdAt is string and update
      val documentIds: Seq[ObjectId] = declarationJourneyRepository.findCreatedAtString(1).futureValue
      val updateTime = declarationJourneyRepository.updateDate(documentIds).futureValue

      // Checks collection sizes and returns are correct
      documentIds.size mustBe 1
      updateTime mustBe 1
    }
  }
}
