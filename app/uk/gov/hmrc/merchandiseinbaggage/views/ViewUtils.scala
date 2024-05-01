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

package uk.gov.hmrc.merchandiseinbaggage.views

import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Json
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.Import
import uk.gov.hmrc.merchandiseinbaggage.model.api.{Country, Declaration, NotRequired, Paid, TotalCalculationResult, YesNoDontKnow}
import uk.gov.hmrc.merchandiseinbaggage.service.CountryService
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._
object ViewUtils {

  def title(form: Form[_], titleStr: String, section: Option[String] = None, titleMessageArgs: Seq[String] = Seq())(
    implicit messages: Messages
  ): String =
    titleNoForm(s"${errorPrefix(form)} ${messages(titleStr, titleMessageArgs: _*)}", section)

  def titleNoForm(title: String, section: Option[String] = None, titleMessageArgs: Seq[String] = Seq())(implicit
    messages: Messages
  ): String =
    s"${messages(title, titleMessageArgs: _*)} - ${section
      .fold("")(messages(_) + " - ")}${messages("service.name")} - ${messages("site.govuk")}"

  private def errorPrefix(form: Form[_])(implicit messages: Messages): String =
    if (form.hasErrors || form.hasGlobalErrors) messages("error.browser.title.prefix") else ""

  def exportCountriesJson(implicit messages: Messages): String =
    Json.toJson(exportCountries.map(_.toAutoCompleteJson)).toString

  lazy val exportCountries: List[Country] =
    CountryService.getAllCountries.filterNot(_.code == "GB")

  def proofOfOriginNeeded(declaration: Declaration, isAssistedDigital: Boolean): Boolean = {
    def calcAmount(maybeTotalCalculationResult: Option[TotalCalculationResult]): Long =
      maybeTotalCalculationResult.fold(0L) {
        _.calculationResults.calculationResults
          .filter(_.goods.producedInEu == YesNoDontKnow.Yes)
          .map(_.gbpAmount.value)
          .sum
      }

    if (!isAssistedDigital && declaration.declarationType == Import) {
      (calcAmount(declaration.maybeTotalCalculationResult) + declaration.amendments
        .filter(amendment => List(Some(Paid), Some(NotRequired)).contains(amendment.paymentStatus))
        .map(x => calcAmount(x.maybeTotalCalculationResult))
        .sum) > 100000L //amount in pence so 100000L == £1000
    } else {
      false
    }
  }
}
