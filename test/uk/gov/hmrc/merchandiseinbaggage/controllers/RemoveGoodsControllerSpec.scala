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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.navigation._
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.RemoveGoodsView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class RemoveGoodsControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  val repo: DeclarationJourneyRepository = app.injector.instanceOf[DeclarationJourneyRepository]
  val view: RemoveGoodsView              = app.injector.instanceOf[RemoveGoodsView]
  val mockNavigator: Navigator           = mock(classOf[Navigator])
  val controller: RemoveGoodsController  =
    new RemoveGoodsController(controllerComponents, actionBuilder, repo, mockNavigator, view)

  "delegate to navigator for navigation in" in {
    givenADeclarationJourneyIsPersisted(completedDeclarationJourney)

    val postReq =
      buildPost(
        RemoveGoodsController.onPageLoad(1).url,
        completedDeclarationJourney.sessionId,
        completedDeclarationJourney,
        formData = Seq("value" -> "Yes")
      )

    when(mockNavigator.nextPage(any[RemoveGoodsRequest])(any[ExecutionContext]))
      .thenReturn(Future.successful(CheckYourAnswersController.onPageLoad))

    val result: Future[Result] = controller.onSubmit(1)(postReq)

    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some("/declare-commercial-goods/check-your-answers")
  }
}
