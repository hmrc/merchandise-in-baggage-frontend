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

package uk.gov.hmrc.merchandiseinbaggage.service

import uk.gov.hmrc.merchandiseinbaggage.model.api.MibReference

import scala.util.Random

trait MibReferenceGenerator {

  protected val referenceFormat = """^X([A-Z])MB(\d{10})$"""

  def mibReference: MibReference = {
    val digits: List[Int] = (0 to 9).toList

    val randomDigits: List[Int] = digits.flatMap { _ =>
      val idx = randomIndex(digits.size)
      digits.find(_ == idx)
    }

    val randomCapital = Random.alphanumeric.filter(c => c.isLetter && c.isUpper).take(1).headOption.getOrElse('A')

    MibReference(s"X${randomCapital}MB${randomDigits.mkString}")
  }

  private def randomIndex(size: Int): Int = Random.nextInt(size)
}
