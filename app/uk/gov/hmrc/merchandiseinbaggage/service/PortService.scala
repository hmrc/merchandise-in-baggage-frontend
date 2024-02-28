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

import uk.gov.hmrc.merchandiseinbaggage.model.api.Port

object PortService {

  def getAllPorts: List[Port] = ports

  def getPortByCode(code: String): Option[Port] = ports.find(c => c.code == code)

  def isValidPortCode(code: String): Boolean = getPortByCode(code).isDefined

  private val ports = List(
    Port("ABZ", "title.aberdeen_airport", isGB = true, Nil),
    Port("ABD", "title.aberdeen_port", isGB = true, Nil),
    Port("AFK", "title.ashford", isGB = true, Nil),
    Port("AVO", "title.avonmouth", isGB = true, Nil),
    Port("BAR", "title.barrow", isGB = true, Nil),
    Port("BEL", "title.belfast_docks", isGB = false, Nil),
    Port("BFS", "title.belfast_international_airport", isGB = false, List("BFS")),
    Port("BWK", "title.berwick_upon_tweed", isGB = true, Nil),
    Port("BQH", "title.biggin_hill_airport", isGB = true, List("London Biggin Hill Airport", "BQH")),
    Port("BRK", "title.birkenhead", isGB = true, Nil),
    Port("BHX", "title.birmingham_airport", isGB = true, List("BHX")),
    Port("BLK", "title.blackpool_airport", isGB = true, List("BLK")),
    Port("BLY", "title.blyth", isGB = true, Nil),
    Port("BOH", "title.bournemouth_airport", isGB = true, List("BOH")),
    Port("BRS", "title.bristol_airport", isGB = true, List("BRS")),
    Port("BZN", "title.brize_norton", isGB = true, List("RAF Brize Norton", "BZZ")),
    Port("BUC", "title.buckie", isGB = true, Nil),
    Port("BTL", "title.burntisland", isGB = true, Nil),
    Port("CYN", "title.cairnryan", isGB = true, List("Stranraer")),
    Port("CBZ", "title.cambridge_city_airport", isGB = true, List("Cambridge Airport", "CBG")),
    Port("CWL", "title.cardiff_airport", isGB = true, List("CWL")),
    Port("CAX", "title.carlisle_lake_district_airport", isGB = true, List("Carlisle Airport", "CAX")),
    Port("LDY", "title.city_of_derry_airport", isGB = false, List("Londonderry Airport", "LDY", "Derry Airport")),
    Port("CVT", "title.coventry_airport", isGB = true, List("CVT")),
    Port("LDY", "title.derry_port", isGB = false, List("Foyle Port", "Londonderry Port")),
    Port("DSA", "title.doncaster_sheffield_airport", isGB = true, List("Robin Hood Airport", "DSA")),
    Port("DVR", "title.dover", isGB = true, List("Port of Dover")),
    Port("DUN", "title.dundee_airport", isGB = true, List("DND")),
    Port("EMA", "title.east_midlands_airport", isGB = true, List("EMA")),
    Port("EBD", "title.ebbsfleet", isGB = true, Nil),
    Port("EDI", "title.edinburgh_airport", isGB = true, List("EDI")),
    Port("EXT", "title.exeter_airport", isGB = true, List("EXT")),
    Port("FAL", "title.falmouth", isGB = true, Nil),
    Port("FAB", "title.farnborough_airport", isGB = true, List("FAB")),
    Port("FXT", "title.felixstowe", isGB = true, Nil),
    Port("FNT", "title.finnart", isGB = true, Nil),
    Port("FIS", "title.fishguard", isGB = true, Nil),
    Port("FLE", "title.fleetwood", isGB = true, Nil),
    Port("FOL", "title.folkestone", isGB = true, List("Cheriton", "Folkestone", "Eurotunnel", "Channel Tunnel")),
    Port("FWM", "title.fort_william_corpach", isGB = true, Nil),
    Port("FRB", "title.fraserburgh", isGB = true, Nil),
    Port("LGW", "title.gatwick_airport", isGB = true, List("London Gatwick Airport", "LGW")),
    Port("BHD", "title.george_best_belfast_city_airport", isGB = false, List("Belfast City Airport", "BHD")),
    Port("GLA", "title.glasgow_airport", isGB = true, List("Glasgow International Airport", "GLA")),
    Port("GLW", "title.glasgow_docks", isGB = true, Nil),
    Port("PIK", "title.glasgow_prestwick_airport", isGB = true, List("Prestwick Airport", "PIK")),
    Port("GSA", "title.glensanda", isGB = true, Nil),
    Port("GRG", "title.grangemouth", isGB = true, Nil),
    Port("GRK", "title.greenock_ocean_terminal", isGB = true, List("Greenock Port")),
    Port("GRI", "title.grimsby", isGB = true, Nil),
    Port("HTP", "title.hartlepool", isGB = true, Nil),
    Port("HRH", "title.harwich", isGB = true, Nil),
    Port("HED", "title.headcorn_aerodrome", isGB = true, Nil),
    Port("LHR", "title.heathrow_airport", isGB = true, List("London Heathrow Airport", "LHR")),
    Port("HEY", "title.heysham", isGB = true, Nil),
    Port("HLD", "title.holyhead", isGB = true, Nil),
    Port("HUL", "title.hull", isGB = true, Nil),
    Port("HUY", "title.humberside_airport", isGB = true, List("HUY")),
    Port("HST", "title.hunterston", isGB = true, Nil),
    Port("IMM", "title.immingham", isGB = true, Nil),
    Port("IVG", "title.invergordon", isGB = true, Nil),
    Port("INK", "title.inverkeithing", isGB = true, Nil),
    Port("INV", "title.inverness_airport", isGB = true, List("INV")),
    Port("INV", "title.inverness_port", isGB = true, Nil),
    Port("IPS", "title.ipswich", isGB = true, Nil),
    Port("IOM", "title.isle_of_man_airport", isGB = true, List("Ronaldsway Airport", "IOM")),
    Port("IOM", "title.isle_of_man_sea_terminal", isGB = true, List("Douglas")),
    Port("KLN", "title.kings_lynn", isGB = true, Nil),
    Port("KOI", "title.kirkwall_airport", isGB = true, List("KOI")),
    Port("LAR", "title.larne", isGB = false, Nil),
    Port("LBA", "title.leeds_bradford_airport", isGB = true, List("Leeds Airport", "Bradford Airport", "LBA")),
    Port("LEI", "title.leith", isGB = true, Nil),
    Port("LER", "title.lerwick", isGB = true, Nil),
    Port(
      "LPL",
      "title.liverpool_john_lennon_airport",
      isGB = true,
      List("Liverpool Airport", "John Lennon Airport", "LPL")
    ),
    Port("LCY", "title.london_city_airport", isGB = true, List("LCY")),
    Port("LGP", "title.london_gateway", isGB = true, Nil),
    Port("LTN", "title.luton_airport", isGB = true, List("London Luton Airport", "LTN")),
    Port("LYX", "title.lydd_airport", isGB = true, List("London Ashford Airport", "LYX")),
    Port("MAN", "title.manchester_airport", isGB = true, List("MAN")),
    Port("MRY", "title.maryport", isGB = true, Nil),
    Port("MTH", "title.methil", isGB = true, Nil),
    Port("MON", "title.montrose", isGB = true, Nil),
    Port("NCL", "title.newcastle_airport", isGB = true, List("Newcastle International Airport", "NCL")),
    Port("NHV", "title.newhaven", isGB = true, Nil),
    Port("NQY", "title.newquay_airport", isGB = true, List("NQY")),
    Port("NSH", "title.north_shields", isGB = true, Nil),
    Port("NHT", "title.northolt", isGB = true, List("RAF Northolt", "NHT")),
    Port("NWI", "title.norwich_airport", isGB = true, List("NWI")),
    Port("PEL", "title.peel", isGB = true, Nil),
    Port("PED", "title.pembroke_port", isGB = true, Nil),
    Port("PHD", "title.peterhead", isGB = true, Nil),
    Port("PLY", "title.plymouth", isGB = true, Nil),
    Port("POO", "title.poole", isGB = true, Nil),
    Port("DUN", "title.port_of_dundee", isGB = true, List("Dundee Port")),
    Port("LIV", "title.port_of_liverpool", isGB = true, List("Liverpool Port")),
    Port("STN", "title.port_of_southampton", isGB = true, List("Southampton Port", "STN")),
    Port("TYN", "title.port_of_tyne", isGB = true, List("Tyne Port")),
    Port("PTM", "title.portsmouth", isGB = true, Nil),
    Port("PUR", "title.purfleet", isGB = true, Nil),
    Port("RSY", "title.ramsey", isGB = true, Nil),
    Port("RMG", "title.ramsgate", isGB = true, Nil),
    Port("RER", "title.redcar", isGB = true, Nil),
    Port("ROS", "title.rosyth", isGB = true, List("Port of Rosyth")),
    Port("SEA", "title.seaham", isGB = true, Nil),
    Port("ESH", "title.shoreham_airport", isGB = true, List("Brighton City Airport", "ESH")),
    Port("SIL", "title.silloth", isGB = true, Nil),
    Port("SOU", "title.southampton_airport", isGB = true, List("SOU")),
    Port("SEN", "title.southend_airport", isGB = true, List("London Southend Airport", "SEN")),
    Port("STP", "title.st_pancras", isGB = true, List("London St Pancras")),
    Port("LSA", "title.stansted_airport", isGB = true, List("London Stansted Airport", "LSA")),
    Port("STY", "title.stornoway_airport", isGB = true, List("STY")),
    Port("LSI", "title.sumburgh_airport", isGB = true, List("LSI")),
    Port("SUN", "title.sunderland", isGB = true, Nil),
    Port("SWA", "title.swansea", isGB = true, Nil),
    Port("MME", "title.teeside_international_airport", isGB = true, List("Durham Tees Valley Airport", "MME")),
    Port("TEE", "title.teesport", isGB = true, Nil),
    Port("MED", "title.thames_gateway_Sheerness", isGB = true, Nil),
    Port("THP", "title.thames_port", isGB = true, Nil),
    Port("TIL", "title.tilbury", isGB = true, List("Port of Tilbury")),
    Port("UNT", "title.unst", isGB = true, Nil),
    Port("WPT", "title.warrenpoint", isGB = false, Nil),
    Port("WEY", "title.weymouth", isGB = true, Nil),
    Port("WTB", "title.whitby", isGB = true, Nil),
    Port("WHV", "title.whitehaven", isGB = true, Nil),
    Port("WIC", "title.wick", isGB = true, Nil),
    Port("WOR", "title.workington", isGB = true, Nil)
  )
}
