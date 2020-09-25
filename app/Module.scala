/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

import com.google.inject.{AbstractModule, Provides}
import javax.inject.Singleton
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DB

class Module extends AbstractModule {

  @Provides
  @Singleton
  def mongoDB(reactiveMongoComponent: ReactiveMongoComponent): () => DB = reactiveMongoComponent.mongoConnector.db
}
