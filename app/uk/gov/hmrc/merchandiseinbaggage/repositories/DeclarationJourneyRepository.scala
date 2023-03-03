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

import com.mongodb.client.model.Indexes.ascending
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.ReturnDocument.AFTER
import org.mongodb.scala.model.{FindOneAndReplaceOptions, IndexModel, IndexOptions}
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.model.api.SessionId
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney.{format, id}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class DeclarationJourneyRepository @Inject()(mongo: MongoComponent, val appConfig: AppConfig)
    extends PlayMongoRepository[DeclarationJourney](
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
}
