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

package uk.gov.hmrc.merchandiseinbaggage.repositories

import com.mongodb.client.model.Indexes.ascending
import org.mongodb.scala.model.Filters.equal
import org.bson.BsonType
import org.bson.types.ObjectId
import org.mongodb.scala.model.ReturnDocument.AFTER
import org.mongodb.scala.model.{Filters, FindOneAndReplaceOptions, IndexModel, IndexOptions, Projections, UpdateOptions, Updates}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney.{format, id}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.result.UpdateResult
import play.api.libs.json._
import uk.gov.hmrc.mongo.play.json.formats.MongoFormats.objectIdFormat

import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import scala.util.Try

@Singleton
class DeclarationJourneyRepository @Inject() (mongo: MongoComponent, val appConfig: AppConfig)(implicit
  ec: ExecutionContext
) extends PlayMongoRepository[DeclarationJourney](
      mongo,
      "declarationJourney",
      format,
      Seq(
        IndexModel(ascending(id), IndexOptions().name("primaryKey").unique(true)),
        IndexModel(
          ascending("createdAt"),
          IndexOptions().name("timeToLive").unique(false).expireAfter(appConfig.mongoTTL, TimeUnit.SECONDS)
        )
      )
    ) {

  def findBySessionId(sessionId: SessionId): Future[Option[DeclarationJourney]] =
    collection.find(equal(id, sessionId.value)).toFuture().map(_.headOption)

  def upsert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] =
    collection
      .findOneAndReplace(
        equal(id, declarationJourney.sessionId.value),
        declarationJourney,
        FindOneAndReplaceOptions().upsert(true).returnDocument(AFTER)
      )
      .toFuture()

  def findCreatedAtString(updateLimit: Int): Future[Seq[ObjectId]] = {
    implicit val bsonObjectIdFormat: Format[ObjectId] = objectIdFormat
    val longReads: Reads[ObjectId]                    = (__ \ "_id").read[ObjectId]
    val projection                                    = Projections.include("_id")
    collection
      .find[BsonDocument](Filters.bsonType("createdAt", BsonType.STRING))
      .limit(updateLimit)
      .projection(projection)
      .map(bson => Codecs.fromBson[ObjectId](bson)(longReads))
      .toFuture()
  }

  def updateDate(documentIds: Seq[ObjectId]): Future[Long] = {
    val time = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
    val zdt  = ZonedDateTime.of(time, ZoneId.systemDefault())

    val selector = Filters.in("_id", documentIds: _*)
    val update   = Updates.set("createdAt", BsonDocument("$toDate" -> zdt.toInstant.toEpochMilli))
    val options  = UpdateOptions().upsert(false)
    val result   = collection.updateMany(selector, Seq(update), options).toFuture()
    result
      .map { updateResult: UpdateResult =>
        Try(updateResult.getModifiedCount).getOrElse(0L)
      }
      .recover { case e: Exception =>
        throw new RuntimeException(s"Failed to add createdAt field: ${e.getMessage}")
      }
  }
}
