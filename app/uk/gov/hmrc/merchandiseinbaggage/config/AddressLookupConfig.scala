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

package uk.gov.hmrc.merchandiseinbaggage.config

import play.api.libs.json.{JsObject, Json}

object AddressLookupConfig {

  def configAddressLookup(continueUrl: String): JsObject =
    Json
      .parse(s"""{
                  |  "version": 2,
                  |  "options": {
                  |    "continueUrl": "$continueUrl"
                  |  },
                  |  "labels": {
                  |    "en": {
                  |      "appLevelLabels": {
                  |        "navTitle": "Declare commercial goods carried in accompanied baggage or small vehicles"
                  |      }
                  |    },
                  |    "cy": {
                  |      "appLevelLabels": {
                  |        "navTitle": "Datgan nwyddau masnachol sy’n cael eu cario mewn bagiau neu gerbydau bach"
                  |      }
                  |    }
                  |  }
                  |}
                  |""".stripMargin)
      .as[JsObject]
}
