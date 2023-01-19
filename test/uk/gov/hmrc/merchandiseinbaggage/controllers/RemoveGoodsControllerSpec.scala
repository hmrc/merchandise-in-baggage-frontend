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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import org.scalamock.scalatest.MockFactory
import play.api.mvc.Result
import play.api.test.Helpers._
import play.mvc.Http.Status
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.RemoveGoodsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class RemoveGoodsControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData with MockFactory {

  val repo = app.injector.instanceOf[DeclarationJourneyRepository]
  val view = app.injector.instanceOf[RemoveGoodsView]
  val mockNavigator = mock[Navigator]
  val controller = new RemoveGoodsController(controllerComponents, actionBuilder, repo, mockNavigator, view)

  "delegate to navigator for navigation in" in {
    givenADeclarationJourneyIsPersisted(completedDeclarationJourney)
    val postReq = buildPost(RemoveGoodsController.onPageLoad(1).url, completedDeclarationJourney.sessionId)
      .withFormUrlEncodedBody("value" -> "Yes")
    val result: Future[Result] = controller.onSubmit(1)(postReq)

    (mockNavigator
      .nextPage(_: RemoveGoodsRequest)(_: ExecutionContext))
      .expects(*, *)
      .returning(Future.successful(CheckYourAnswersController.onPageLoad))

    status(result) mustBe Status.SEE_OTHER
  }
}
