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

package uk.gov.hmrc.merchandiseinbaggage.generators

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen.{alphaStr, choose}

trait Generators {

  def nonBooleans: Gen[String] =
    arbitrary[String]
      .suchThat(_.nonEmpty)
      .suchThat(_ != "true")
      .suchThat(_ != "false")

  def nonYesNos: Gen[String] =
    arbitrary[String]
      .suchThat(_.nonEmpty)
      .suchThat(_ != "Yes")
      .suchThat(_ != "No")

  def nonEmptyString: Gen[String] = arbitrary[String] suchThat (_.nonEmpty)

  def nonNumerics: Gen[String] = alphaStr suchThat (_.nonEmpty)

  def positiveBigDecimalsWithMaximum3dp: Gen[BigDecimal] =
    arbitrary[BigDecimal] retryUntil (bd => bd > 0 && bd.scale <= 3)

  def negativeBigDecimals: Gen[BigDecimal] =
    arbitrary[BigDecimal] retryUntil (bd => bd < 0)

  def positiveBigDecimalsWithMoreThan3dp: Gen[BigDecimal] =
    arbitrary[BigDecimal] retryUntil (bd => bd > 0 && bd.scale > 3)

  def stringsExceptSpecificValues(excluded: Seq[String]): Gen[String] =
    nonEmptyString suchThat (!excluded.contains(_))

  def oneOf[T](xs: Seq[Gen[T]]): Gen[T] =
    if (xs.isEmpty) {
      throw new IllegalArgumentException("oneOf called on empty collection")
    } else {
      val vector = xs.toVector
      choose(0, vector.size - 1).flatMap(vector(_))
    }
}
