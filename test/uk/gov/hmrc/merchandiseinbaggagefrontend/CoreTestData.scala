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

package uk.gov.hmrc.merchandiseinbaggagefrontend

import uk.gov.hmrc.merchandiseinbaggagefrontend.controllers.testonly.TestOnlyController
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api._
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.{Declaration, DeclarationJourney, GoodsEntry, SessionId}

trait CoreTestData {
  val payApiRequest: PayApiRequest = PayApiRequest(
    MibReference("MIBI1234567890"),
    AmountInPence(1),
    AmountInPence(2),
    AmountInPence(3),
    TraderDetails("Trader Inc, 239 Old Street, Berlin, Germany, EC1V 9EY"),
    MerchandiseDetails("Parts and technical crew for the forest moon")
  )

  val sessionId: SessionId = SessionId()

  val startedDeclarationJourney: DeclarationJourney = DeclarationJourney(sessionId)

  val completedGoodsEntry: GoodsEntry = TestOnlyController.completedGoodsEntry

  val completedDeclarationJourney: DeclarationJourney = TestOnlyController.sampleDeclarationJourney(sessionId)

  val declaration: Declaration = completedDeclarationJourney.declarationIfRequiredAndComplete.get

  val incompleteDeclarationJourney: DeclarationJourney = completedDeclarationJourney.copy(maybeJourneyDetails = None)
}
