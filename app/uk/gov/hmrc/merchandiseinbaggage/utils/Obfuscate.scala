/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggage.utils

import uk.gov.hmrc.merchandiseinbaggage.model.api.addresslookup.{Address, AddressLookupCountry}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Email, Eori, Name}
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney

trait Obfuscate[A] {
  def obfuscated(a: A): A
}

object Obfuscate {

  implicit val eoriObfuscate: Obfuscate[Eori] = eori => Eori(eori.value.obfuscated)

  implicit val nameObfuscate: Obfuscate[Name] =
    name => Name(name.firstName.obfuscated, name.lastName.obfuscated)

  implicit val emailObfuscate: Obfuscate[Email] =
    email => Email(email.email.obfuscated)

  implicit val addressCountryObfuscate: Obfuscate[AddressLookupCountry] = address => {
    import address._
    AddressLookupCountry(code.obfuscated, name.obfuscated)
  }

  implicit val addressObfuscate: Obfuscate[Address] = address => {
    import address._
    Address(lines.map(line => line.obfuscated), postcode.obfuscated, country.obfuscated)
  }

  implicit val stringObfuscate: Obfuscate[String] = value => value.flatMap(_ => "*")

  implicit val optionStringObfuscate: Obfuscate[Option[String]] =
    value => value.map(string => string.obfuscated)

  implicit val declarationJourneyObfuscate: Obfuscate[DeclarationJourney] =
    declarationJourney => {
      import declarationJourney._
      declarationJourney.copy(
        maybeNameOfPersonCarryingTheGoods = maybeNameOfPersonCarryingTheGoods.map(_.obfuscated),
        maybeEmailAddress = maybeEmailAddress.map(_.obfuscated),
        maybeCustomsAgentName = maybeCustomsAgentName.obfuscated,
        maybeCustomsAgentAddress = maybeCustomsAgentAddress.map(_.obfuscated),
        maybeEori = maybeEori.map(_.obfuscated),
        maybeRegistrationNumber = maybeRegistrationNumber.obfuscated
      )
    }

  implicit class DataObfuscate[T](t: T) {
    def obfuscated(implicit obfuscate: Obfuscate[T]): T = obfuscate.obfuscated(t)
  }
}
