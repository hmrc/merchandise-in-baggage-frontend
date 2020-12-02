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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages

import org.openqa.selenium.WebDriver
import org.scalatestplus.selenium.WebBrowser

class CheckYourAnswersPage(implicit webDriver: WebDriver) extends BasePage {

  import WebBrowser._

  def clickOnAcceptAndPayButton(): String = {
    val button = find(NameQuery("payButton")).get
    click on button

    readPath()
  }

  def clickOnMakeDeclarationButton(): String = {
    val button = find(NameQuery("makeDeclarationButton")).get
    click on button

    readPath()
  }

  def clickOnChangeGoodsTypeLink(index: Int): String = clickOnChangeLink(s"categoryChangeLink_$index")

  def clickOnChangeGoodsQuantityLink(index: Int): String = clickOnChangeLink(s"quantityChangeLink_$index")

  def clickOnChangeVatRateLink(index: Int): String = clickOnChangeLink(s"vatRateChangeLink_$index")

  def clickOnChangeGoodsCountryLink(index: Int): String = clickOnChangeLink(s"countryChangeLink_$index")

  def clickOnChangePurchaseDetailsLink(index: Int): String = clickOnChangeLink(s"priceChangeLink_$index")

  def clickOnChangeCustomsAgentLink(): String = clickOnChangeLink("customsAgentNameChangeLink")

  def clickOnChangeEoriLink(): String = clickOnChangeLink("eoriChangeLink")

  def clickOnChangeTravellerDetailsLink(): String = clickOnChangeLink("nameOfPersonCarryingTheGoodsChangeLink")

  def clickOnChangeEmailAddressLink(): String = clickOnChangeLink("emailAddressChangeLink")

  def clickOnPlaceOfArrivalLink(): String = clickOnChangeLink("placeOfArrivalChangeLink")

  def clickOnDateOfArrivalLink(): String = clickOnChangeLink("dateOfArrivalChangeLink")

  def clickOnTravellingByVehicleChangeLink(): String = clickOnChangeLink("travellingByVehicleChangeLink")

  def clickOnVehicleRegistrationNumberChangeLink(): String = clickOnChangeLink("vehicleRegistrationNumberChangeLink")

  def clickOnRemoveGoodsLink(index: Int): String = clickOnChangeLink(s"removeGoodsLink_$index")

  def clickOnAddMoreGoodsLink(): String = clickOnChangeLink("addMoreGoodsLink")

  private def clickOnChangeLink(id: String) = {
    val changeLink = find(IdQuery(id)).get
    click on changeLink
    readPath()
  }
}

object CheckYourAnswersPage {
  val path = "/declare-commercial-goods/check-your-answers"
  val title = "Check your answers before making your declaration"

  val expectedSectionHeaders =
    Seq("Details of the goods", "Personal details", "Journey details", "Now send your declaration")
}