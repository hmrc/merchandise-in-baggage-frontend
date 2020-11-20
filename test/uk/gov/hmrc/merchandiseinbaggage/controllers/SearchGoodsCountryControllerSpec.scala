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
import uk.gov.hmrc.merchandiseinbaggage.views.html.SearchGoodsCountryView

import scala.concurrent.ExecutionContext.Implicits.global

class SearchGoodsCountryControllerSpec extends DeclarationJourneyControllerSpec {
  "onSubmit" must {
    "return BAD_REQUEST and errors" when {
      "no selection is made" in {
        val controller =
          new SearchGoodsCountryController(
            controllerComponents, actionBuilder, declarationJourneyRepository, injector.instanceOf[SearchGoodsCountryView])

        givenADeclarationJourneyIsPersisted(importJourneyWithStartedGoodsEntry)

        val result = controller.onSubmit(1)(buildPost(routes.SearchGoodsCountryController.onSubmit(1).url, sessionId))
        val content = contentAsString(result)

        status(result) mustEqual BAD_REQUEST

        content must include(s"In what country did you buy the ${startedGoodsEntry.maybeCategoryQuantityOfGoods.get.category}?")
        content must include("If you bought this item on a plane or boat, enter the country you were travelling from at the time of purchase")
        content must include("Continue")
      }
    }
  }
}
