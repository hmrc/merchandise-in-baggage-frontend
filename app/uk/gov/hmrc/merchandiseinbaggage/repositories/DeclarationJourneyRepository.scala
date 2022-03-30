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

package uk.gov.hmrc.merchandiseinbaggage.repositories

import com.mongodb.client.model.Indexes.ascending
import org.mongodb.scala.model.Filters.{empty, equal}
import org.mongodb.scala.model.ReturnDocument.AFTER
import org.mongodb.scala.model.{FindOneAndReplaceOptions, IndexModel, IndexOptions}
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney.{format, id}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeclarationJourneyRepository @Inject()(mongo: MongoComponent, @Named("declarationJourneyTimeToLiveInSeconds") ttlInSeconds: Int)
    extends PlayMongoRepository[DeclarationJourney](
      mongo,
      "declarationJourney",
      format,
      Seq(
        IndexModel(ascending(id), IndexOptions().name("primaryKey").unique(true)),
        IndexModel(
          ascending("createdAt"),
          IndexOptions().name("timeToLive").unique(false).expireAfter(ttlInSeconds, TimeUnit.SECONDS)
        )
      )
    ) with Logging {

  private[repositories] lazy val timeToLiveInSeconds: Int = ttlInSeconds

  private[repositories] lazy val indices = collection.listIndexes().toFuture()

  def insert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] =
    collection.insertOne(declarationJourney).toFuture().map(_ => declarationJourney)

  def findBySessionId(sessionId: SessionId): Future[Option[DeclarationJourney]] =
    collection.find(equal(id, sessionId.value)).toFuture().map(_.headOption)

  def findAll(): Future[Seq[DeclarationJourney]] = collection.find().toFuture()

  def deleteAll(): Future[Unit] = {
    logger.warn("DeclarationJourneyRepository.deleteAll() called")
    collection.deleteMany(empty()).toFuture().map(_ => ())
  }

  implicit val jsObjectWriter: OWrites[JsObject] = new OWrites[JsObject] {
    override def writes(o: JsObject): JsObject = o
  }

  /**
    * Update or Insert (UpSert)
    */
  def upsert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] =
    collection
      .findOneAndReplace(
        equal(id, declarationJourney.sessionId.value),
        declarationJourney,
        FindOneAndReplaceOptions().upsert(true).returnDocument(AFTER)
      )
      .toFuture()
}
