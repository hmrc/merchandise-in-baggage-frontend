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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.test.Helpers._
import play.mvc.Http.Status
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo._
import uk.gov.hmrc.merchandiseinbaggage.model.core.GoodsEntries
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.RemoveGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class RemoveGoodsControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  val repo = app.injector.instanceOf[DeclarationJourneyRepository]
  val view = app.injector.instanceOf[RemoveGoodsView]
  val controller = new RemoveGoodsController(controllerComponents, actionBuilder, repo, view)

  "on submit if answer No" should {
    s"redirect back to ${routes.CheckYourAnswersController.onPageLoad().url} if journey was completed" in {
      val result = controller.removeGoodOrRedirect(1, completedDeclarationJourney, No)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }

    s"redirect back to ${routes.ReviewGoodsController.onPageLoad().url} if journey was NOT completed" in {
      val result = controller.removeGoodOrRedirect(1, startedImportJourney, No)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.ReviewGoodsController.onPageLoad().url)
    }
  }

  "on submit if answer yes" should {
    s"redirect to ${routes.GoodsRemovedController.onPageLoad()} if goods contains an entry" in {
      val result = controller.removeGoodOrRedirect(1, importJourneyWithStartedGoodsEntry, Yes)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.GoodsRemovedController.onPageLoad().url)
    }

    s"redirect to ${routes.ReviewGoodsController.onPageLoad()} if goods contains more entries" in {
      val journey = importJourneyWithStartedGoodsEntry.copy(goodsEntries = GoodsEntries(Seq(startedImportGoods, startedImportGoods)))
      val result = controller.removeGoodOrRedirect(1, journey, Yes)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.ReviewGoodsController.onPageLoad().url)
    }

    s"redirect to ${routes.CheckYourAnswersController.onPageLoad()} if goods contains more entries and is completed" in {
      val journey = completedDeclarationJourney.copy(goodsEntries = GoodsEntries(Seq(completedImportGoods, completedImportGoods)))
      val result = controller.removeGoodOrRedirect(1, journey, Yes)

      status(result) mustBe Status.SEE_OTHER
      redirectLocation(result) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
    }
  }
}
