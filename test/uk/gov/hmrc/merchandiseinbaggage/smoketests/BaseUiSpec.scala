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

package uk.gov.hmrc.merchandiseinbaggage.smoketests

import org.scalatest.concurrent.Eventually
import org.scalatestplus.selenium.{HtmlUnit, WebBrowser}
import uk.gov.hmrc.merchandiseinbaggage.pagespecs.pages.{BaseUrl, Page}
import uk.gov.hmrc.merchandiseinbaggage.{BaseSpecWithApplication, CoreTestData, WireMockSupport}

import scala.util.{Failure, Success, Try}

class BaseUiSpec extends BaseSpecWithApplication with WireMockSupport with HtmlUnit with Eventually with CoreTestData {

  webDriver.setJavascriptEnabled(false)

  lazy val baseUrl: BaseUrl = BaseUrl(s"http://localhost:$port")

  def goto(path: String): Unit = WebBrowser.goTo(s"${baseUrl.value}$path")

  def getCurrentUrl: String = webDriver.getCurrentUrl

  def fullUrl(path: String) = s"${baseUrl.value}$path"

  def submitPage(page: Page): Unit =
    Try {
      page.submitPage()
    } match {
      case Success(_) => ()
      case Failure(ex) =>
        println(s"currentUrl: ${webDriver.getCurrentUrl}")
        ex.printStackTrace()
        fail()
    }
}
