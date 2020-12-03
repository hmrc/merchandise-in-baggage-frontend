/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.merchandiseinbaggage.model.core.YesNo
import uk.gov.hmrc.merchandiseinbaggage.repositories.DeclarationJourneyRepository
import uk.gov.hmrc.merchandiseinbaggage.views.html.RemoveGoodsView

import scala.concurrent.ExecutionContext.Implicits.global

class RemoveGoodsControllerSpec extends DeclarationJourneyControllerSpec with CoreTestData {

  val repo = app.injector.instanceOf[DeclarationJourneyRepository]
  val view = app.injector.instanceOf[RemoveGoodsView]
  val controller = new RemoveGoodsController(controllerComponents, actionBuilder, repo, view)

  s"on submit redirect back to ${routes.CheckYourAnswersController.onPageLoad().url} if answer No and journey was completed" in {
    val result = controller.removeGoodOrRedirect(1, completedDeclarationJourney, YesNo.No)

    status(result) mustBe Status.SEE_OTHER
    redirectLocation(result) mustBe Some(routes.CheckYourAnswersController.onPageLoad().url)
  }

  s"on submit redirect back to ${routes.ReviewGoodsController.onPageLoad().url} if answer No and journey was not completed" in {
    val result = controller.removeGoodOrRedirect(1, startedImportJourney, YesNo.No)

    status(result) mustBe Status.SEE_OTHER
    redirectLocation(result) mustBe Some(routes.ReviewGoodsController.onPageLoad().url)
  }
}
