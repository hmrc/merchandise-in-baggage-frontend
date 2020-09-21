/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.controllers

import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, MessagesRequest}
import uk.gov.hmrc.merchandiseinbaggagefrontend.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggagefrontend.views.html.SkeletonPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

/**
 * Controller serving skeleton pages
 *
 * It is envisaged this controller will be re-factored away route-by-route as we build out the journey
 */
class SkeletonJourneyController @Inject()(mcc: MessagesControllerComponents, page: SkeletonPage)
                                         (implicit val ec: ExecutionContext, appConfig: AppConfig)
  extends FrontendController(mcc) {

  val traderEori: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    Future.successful(Ok(page(s"trader-eori.title")))
  }

  val traderJourney: Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    Future.successful(Ok(page(s"trader-journey.title")))
  }
}
