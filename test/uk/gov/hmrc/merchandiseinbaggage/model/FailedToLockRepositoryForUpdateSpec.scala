/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FailedToLockRepositoryForUpdateSpec extends AnyWordSpec with Matchers {
  val repositoryUpdateMessage: RepositoryUpdateMessage = FailedToLockRepositoryForUpdate
  "repositoryUpdateMessage" should {
    "have the right message" in {
      repositoryUpdateMessage.message shouldBe s"${repositoryUpdateMessage.prefix} Failed to acquire lock. Update job not completed."
    }
  }
}
