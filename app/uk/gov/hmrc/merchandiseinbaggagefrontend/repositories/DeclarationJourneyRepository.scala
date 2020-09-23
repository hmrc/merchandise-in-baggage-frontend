/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.repositories

import javax.inject.Inject
import play.api.libs.json.{Format, JsString}
import play.api.libs.json.Json.JsValueWrapper
import reactivemongo.api.DB
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.{DeclarationJourney, SessionId}
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration.DeclarationJourney.{format, id}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class DeclarationJourneyRepository @Inject()(mongo: () => DB)
  extends ReactiveRepository[DeclarationJourney, String]("declarationJourney", mongo, format, implicitly[Format[String]]) {

  override def indexes: Seq[Index] = Seq(Index(Seq(s"$id" -> Ascending), Option("primaryKey"), unique = true))

  def insert(declarationJourney: DeclarationJourney): Future[DeclarationJourney] =
    super.insert(declarationJourney).map(_ => declarationJourney)

  def findBySessionId(sessionId: SessionId): Future[Option[DeclarationJourney]] = {
    val query: (String, JsValueWrapper) = s"$id" -> JsString(sessionId.value)
    find(query).map(_.headOption)
  }

  def deleteAll(): Future[Unit] = super.removeAll().map(_ => ())
}
