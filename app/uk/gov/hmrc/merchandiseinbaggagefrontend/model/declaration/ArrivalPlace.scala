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

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.declaration

import enumeratum.{Enum, EnumEntry}
import play.api.libs.json._

import scala.collection.immutable

object EnumFormat {
  def apply[T <: EnumEntry](e: Enum[T]): Format[T] = Format(
    Reads {
      case JsString(value) => e.withNameOption(value).map(JsSuccess(_)).getOrElse(JsError(s"Unknown ${e.getClass.getSimpleName} value: $value"))
      case _ => JsError("Can only parse String")
    },
    Writes(v => JsString(v.entryName)))
}

object PlaceOfArrival {
  implicit val format: Format[PlaceOfArrival] = EnumFormat(PlacesOfArrival)
}

sealed abstract class PlaceOfArrival extends EnumEntry {
  val requiresVehicleChecks: Boolean = false
  val display: String = entryName
}

object PlacesOfArrival extends Enum[PlaceOfArrival] {
  def values: immutable.IndexedSeq[PlaceOfArrival] = findValues

  def forCode(code: String): Option[PlaceOfArrival] = values.find(_.toString == code)

  case object Lerwick extends PlaceOfArrival

  case object Sumburgh extends PlaceOfArrival

  case object Kirkwall extends PlaceOfArrival

  case object Stornoway extends PlaceOfArrival

  case object Wick extends PlaceOfArrival

  case object Invergordon extends PlaceOfArrival

  case object InvernessPort extends PlaceOfArrival {
    override val display: String = "Inverness Port"
  }

  case object InvernessAirport extends PlaceOfArrival {
    override val display: String = "Inverness Airport"
  }

  case object Buckie extends PlaceOfArrival

  case object Fraserburgh extends PlaceOfArrival

  case object Peterhead extends PlaceOfArrival

  case object AberdeenAirport extends PlaceOfArrival {
    override val display: String = "Aberdeen Airport"
  }

  case object AberdeenPort extends PlaceOfArrival {
    override val display: String = "Aberdeen Port"
  }

  case object FortWilliamCorpach extends PlaceOfArrival {
    override val display: String = "Fort William/Corpach"
  }

  case object Glensanda extends PlaceOfArrival

  case object Montrose extends PlaceOfArrival

  case object Dundee extends PlaceOfArrival

  case object Methil extends PlaceOfArrival

  case object Burntisland extends PlaceOfArrival

  case object Inverkeithing extends PlaceOfArrival

  case object Rosyth extends PlaceOfArrival

  case object Grangemouth extends PlaceOfArrival

  case object EdinburghAirport extends PlaceOfArrival {
    override val display: String = "Edinburgh Airport"
  }

  case object Leith extends PlaceOfArrival

  case object BerwickUponTweed extends PlaceOfArrival {
    override val display: String = "Berwick Upon Tweed"
  }

  case object Finnart extends PlaceOfArrival

  case object GlasgowDocks extends PlaceOfArrival {
    override val display: String = "Glasgow Docks"
  }

  case object GreenockOceanTerminal extends PlaceOfArrival {
    override val display: String = "Greenock Ocean Terminal"
  }

  case object GlasgowAirport extends PlaceOfArrival {
    override val display: String = "Glasgow Airport"
  }

  case object Hunterston extends PlaceOfArrival

  case object PrestwickAirport extends PlaceOfArrival {
    override val display: String = "Prestwick Airport"
  }

  case object Derry extends PlaceOfArrival

  case object CityOfDerryAirport extends PlaceOfArrival {
    override val display: String = "City of Derry Airport"
  }

  case object BelfastInternationalAirport extends PlaceOfArrival {
    override val display: String = "Belfast International Airport"
  }

  case object BelfastDocks extends PlaceOfArrival {
    override val display: String = "Belfast Docks"
  }

  case object GeorgeBestBelfastCityAirport extends PlaceOfArrival {
    override val display: String = "George Best Belfast City Airport"
  }

  case object Warrenpoint extends PlaceOfArrival

  case object Whitehaven extends PlaceOfArrival

  case object Workington extends PlaceOfArrival

  case object Maryport extends PlaceOfArrival

  case object Silloth extends PlaceOfArrival

  case object CarlisleAirport extends PlaceOfArrival {
    override val display: String = "Carlisle Airport"
  }

  case object Blyth extends PlaceOfArrival

  case object Newcastle extends PlaceOfArrival

  case object NorthShields extends PlaceOfArrival {
    override val display: String = "North Shields"
  }

  case object PortOfTyne extends PlaceOfArrival {
    override val display: String = "Port of Tyne"
  }

  case object Sunderland extends PlaceOfArrival

  case object Seaham extends PlaceOfArrival

  case object Hartlepool extends PlaceOfArrival

  case object Teesport extends PlaceOfArrival

  case object Redcar extends PlaceOfArrival

  case object DurhamTeesValley extends PlaceOfArrival {
    override val display: String = "Durham Tees Valley"
  }

  case object Whitby extends PlaceOfArrival

  case object Barrow extends PlaceOfArrival

  case object Heysham extends PlaceOfArrival

  case object Fleetwood extends PlaceOfArrival

  case object Blackpool extends PlaceOfArrival

  case object PortOfLiverpool extends PlaceOfArrival {
    override val display: String = "Port of Liverpool"
  }

  case object LiverpoolJohnLennonAirport extends PlaceOfArrival {
    override val display: String = "Liverpool John Lennon Airport"
  }

  case object Birkenhead extends PlaceOfArrival

  case object Holyhead extends PlaceOfArrival

  case object Manchester extends PlaceOfArrival

  case object LeedsBradford extends PlaceOfArrival {
    override val display: String = "Leeds/Bradford"
  }

  case object Hull extends PlaceOfArrival

  case object Humberside extends PlaceOfArrival

  case object Immingham extends PlaceOfArrival

  case object Grimsby extends PlaceOfArrival

  case object DoncasterSheffieldRobinHoodAirport extends PlaceOfArrival {
    override val display: String = "Doncaster Sheffield (Robin Hood Airport)"
  }

  case object EastMidlandsAirport extends PlaceOfArrival {
    override val display: String = "East Midlands Airport"
  }

  case object CoventryInternational extends PlaceOfArrival {
    override val display: String = "Coventry International"
  }

  case object Birmingham extends PlaceOfArrival

  case object Coventry extends PlaceOfArrival

  case object Cambridge extends PlaceOfArrival

  case object KingsLynn extends PlaceOfArrival {
    override val display: String = "King’s Lynn"
  }

  case object Norwich extends PlaceOfArrival

  case object Luton extends PlaceOfArrival

  case object Stansted extends PlaceOfArrival

  case object Harwich extends PlaceOfArrival

  case object Ipswich extends PlaceOfArrival

  case object Felixstowe extends PlaceOfArrival

  case object Southend extends PlaceOfArrival

  case object ThamesPortIsleOfGrain extends PlaceOfArrival {
    override val display: String = "Thames Port (Isle of Grain)"
  }

  case object ThamesGatewaySheerness extends PlaceOfArrival {
    override val display: String = "Thames Gateway (Sheerness)"
  }

  case object Tilbury extends PlaceOfArrival

  case object LondonCity extends PlaceOfArrival {
    override val display: String = "London City"
  }

  case object Purfleet extends PlaceOfArrival

  case object LangleyHeathrowWorldwideDistributionCentre extends PlaceOfArrival {
    override val display: String = "Langley (Heathrow worldwide distribution centre)"
  }

  case object LondonGateway extends PlaceOfArrival {
    override val display: String = "London Gateway"
  }

  case object BrizeNorton extends PlaceOfArrival {
    override val display: String = "Brize Norton"
  }

  case object Northolt extends PlaceOfArrival

  case object Heathrow extends PlaceOfArrival

  case object Farnborough extends PlaceOfArrival

  case object Fishguard extends PlaceOfArrival

  case object PembrokeDocks extends PlaceOfArrival {
    override val display: String = "Pembroke Docks"
  }

  case object Swansea extends PlaceOfArrival

  case object Cardiff extends PlaceOfArrival

  case object Newhaven extends PlaceOfArrival

  case object ShorehamAirport extends PlaceOfArrival {
    override val display: String = "Shoreham Airport"
  }

  case object Portsmouth extends PlaceOfArrival

  case object Bournemouth extends PlaceOfArrival

  case object Poole extends PlaceOfArrival

  case object Weymouth extends PlaceOfArrival

  case object Exeter extends PlaceOfArrival

  case object Plymouth extends PlaceOfArrival

  case object Falmouth extends PlaceOfArrival

  case object Newquay extends PlaceOfArrival

  case object Avonmouth extends PlaceOfArrival

  case object Bristol extends PlaceOfArrival

  case object Southampton extends PlaceOfArrival

  case object BigginHill extends PlaceOfArrival {
    override val display: String = "Biggin Hill"
  }

  case object Gatwick extends PlaceOfArrival

  case object Ebbsfleet extends PlaceOfArrival

  case object Ramsgate extends PlaceOfArrival

  case object Dover extends PlaceOfArrival {
    override val requiresVehicleChecks: Boolean = true
  }

  case object Cheriton extends PlaceOfArrival

  case object Lydd extends PlaceOfArrival

  case object Ashford extends PlaceOfArrival

  case object Headcorn extends PlaceOfArrival

  case object StPancras extends PlaceOfArrival {
    override val display: String = "St Pancras"
  }

  case object Coquelles extends PlaceOfArrival

}
