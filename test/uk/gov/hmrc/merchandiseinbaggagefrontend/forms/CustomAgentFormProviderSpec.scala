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

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpec

class CustomAgentFormProviderSpec extends BaseSpec {

  val form = new CustomAgentFormProvider()

  "Bind CustomsDeclares data or return error" in {
    val bindData = Map("value" -> true.toString)
    val bindDataTwo = Map("value" -> false.toString)

    form().bind(bindData).data mustBe bindData
    form().bind(bindDataTwo).data mustBe bindDataTwo
    form().bind(Map[String, String]()).errors.head.message mustBe "customsDeclares.error.required"
  }
}
