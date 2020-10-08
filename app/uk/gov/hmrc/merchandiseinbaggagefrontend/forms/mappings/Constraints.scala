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

package uk.gov.hmrc.merchandiseinbaggagefrontend.forms.mappings

import play.api.data.validation.{Constraint, Invalid, Valid}

trait Constraints {

  protected def existInList(options: List[String], errorKey: String): Constraint[String] = Constraint { value =>
    if (options.contains(value)) Valid else Invalid(errorKey)
  }

  protected def greaterThan[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] = Constraint { value =>
    import ev._
    if (value > minimum) Valid else Invalid(errorKey)
  }
}
