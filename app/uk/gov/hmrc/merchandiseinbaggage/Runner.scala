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

package uk.gov.hmrc.merchandiseinbaggage

object Runner extends App {

  import java.text.NumberFormat.getCurrencyInstance
  import java.util.Locale.UK

  def inPounds(value: Long): BigDecimal = (BigDecimal(value) / 100).setScale(2)

  getCurrencyInstance(UK).format(inPounds(100.999.toLong)).split("\\.00")(0)

}
