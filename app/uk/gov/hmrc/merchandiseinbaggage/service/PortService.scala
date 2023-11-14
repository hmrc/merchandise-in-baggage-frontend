/*
 * Copyright 2023 HM Revenue & Customs
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

  private val englandAndWalesSeaports = List(
    Port("AFK", "title.ashford", isGB = true, Nil),
    Port("AVO", "title.avonmouth", isGB = true, Nil),
    Port("BAR", "title.barrow", isGB = true, Nil),
    Port("BWK", "title.berwick_upon_tweed", isGB = true, Nil),
    Port("BRK", "title.birkenhead", isGB = true, Nil),
    Port("BLY", "title.blyth", isGB = true, Nil),
    Port("CAX", "title.carlisle_lake_district_airport", isGB = true, List("Carlisle Airport", "CAX")),
    Port("LDY", "title.city_of_derry_airport", isGB = false, List("Londonderry Airport", "LDY", "Derry Airport")),
    Port("DVR", "title.dover", isGB = true, List("Port of Dover")), //dover appears as DOV not DVR
    Port("DUN", "title.dundee_airport", isGB = true, List("DND")),
    Port("EBD", "title.ebbsfleet", isGB = true, Nil),
    Port("FAL", "title.falmouth", isGB = true, Nil),
    Port("FXT", "title.felixstowe", isGB = true, Nil),
    Port("FIS", "title.fishguard", isGB = true, Nil),
    Port("FLE", "title.fleetwood", isGB = true, Nil),
    Port("FOL", "title.folkestone", isGB = true, List("Cheriton", "Folkestone", "Eurotunnel", "Channel Tunnel")),
    Port("GSA", "title.glensanda", isGB = true, Nil),
    Port("GRI", "title.grimsby", isGB = true, Nil),
    Port("HTP", "title.hartlepool", isGB = true, Nil),
    Port("HRH", "title.harwich", isGB = true, Nil),
    Port("HED", "title.headcorn_aerodrome", isGB = true, Nil),
    Port("HEY", "title.heysham", isGB = true, Nil),
    Port("HLD", "title.holyhead", isGB = true, Nil),
    Port("HUL", "title.hull", isGB = true, Nil), // shouldnt this be Bridlington?
    Port("IMM", "title.immingham", isGB = true, Nil),
    Port("INV", "title.inverness_airport", isGB = true, List("INV")),
    Port("IPS", "title.ipswich", isGB = true, Nil),
    Port("KLN", "title.kings_lynn", isGB = true, Nil),
    Port("KOI", "title.kirkwall_airport", isGB = true, List("KOI")),
    Port("LGP", "title.london_gateway", isGB = true, Nil),
    Port("MRY", "title.maryport", isGB = true, Nil),
    Port("NHV", "title.newhaven", isGB = true, Nil),
    Port("NSH", "title.north_shields", isGB = true, Nil),
    Port("PED", "title.pembroke_port", isGB = true, Nil),
    Port("POO", "title.poole", isGB = true, Nil),
    Port("LIV", "title.port_of_liverpool", isGB = true, List("Liverpool Port")),
    Port("STN", "title.port_of_southampton", isGB = true, List("Southampton Port")),
    Port("TYN", "title.port_of_tyne", isGB = true, List("Tyne Port")),
    Port("PTM", "title.portsmouth", isGB = true, Nil),
    Port("PUR", "title.purfleet", isGB = true, Nil),
    Port("RMG", "title.ramsgate", isGB = true, Nil),
    Port("RER", "title.redcar", isGB = true, Nil),
    Port("SEA", "title.seaham", isGB = true, Nil),
    Port("SIL", "title.silloth", isGB = true, Nil),
    Port("STP", "title.st_pancras", isGB = true, List("London St Pancras")),
    Port("SUN", "title.sunderland", isGB = true, Nil),
    Port("SWA", "title.swansea", isGB = true, Nil),
    Port("TEE", "title.teesport", isGB = true, Nil),
    Port("MED", "title.thames_gateway_Sheerness", isGB = true, Nil),
    Port("THP", "title.thames_port", isGB = true, Nil),
    Port("TIL", "title.tilbury", isGB = true, List("Port of Tilbury")),
    Port("UNT", "title.unst", isGB = true, Nil),
    Port("WEY", "title.weymouth", isGB = true, Nil), // shouldnt this be Bridport?
    Port("WTB", "title.whitby", isGB = true, Nil),
    Port("WHV", "title.whitehaven", isGB = true, Nil),
    Port("WOR", "title.workington", isGB = true, Nil)
  )

  private val isleOfManSeaports = List(
    //missing: Port("DGS", "title.castletown_port", isGB = true, List("DGS (Douglas)")),
    Port("IOM", "title.isle_of_man_sea_terminal", isGB = true, List("Douglas", "DGS")),
    Port("PEL", "title.peel", isGB = true, List("DGS (Douglas)")),
    Port("RSY", "title.ramsey", isGB = true, List("DGS (Douglas)")),
  )

  private val channelIslandsSeaports = List(
    //missing: Port("JSY", "title.st_helier_port", isGB = true, List("JSY (Jersey)")),
    //missing: Port("GSY", "title.st_peter_port", isGB = true, List("GSY (Guernsey)"))
  )

  private val scotlandSeaports = List(
    Port("ABD", "title.aberdeen_port", isGB = true, Nil),
    //missing: Port("GRK", "title.annan_docks", isGB = true, List("GRK (Greenock)")),
    //missing: Port("DUN", "title.arbroath_docks", isGB = true, List("DUN (Dundee)")),
    //missing: Port("ARD", "title.ardrossan_docks", isGB = true, Nil),
    //missing: Port("AYR", "title.ayr_docks", isGB = true, Nil),
    //missing: Port("GRK", "title.barcaldine_docks", isGB = true, List("GRK (Greenock)")),
    //missing: Port("GRG", "title.braefood_bay_docks", isGB = true, List("GRG (Grangemouth)")),
    //missing: Port("GLW", "title.bowling_docks", isGB = true, List("GLW (Glasgow)")),
    Port("BUC", "title.buckie", isGB = true, List("FRB (Fraserburgh)")),
    //missing: Port("INV", "title.burghead_docks", isGB = true, List("INV (Inverness)")),
    Port("BTL", "title.burntisland", isGB = true, List("GRG (Grangemouth)")),
    //missing: Port("GRK", "title.campbeltown_docks", isGB = true, List("GRK (Greenock)")),
    Port("FWM", "title.fort_william_corpach", isGB = true, Nil),
    Port("DUN", "title.port_of_dundee", isGB = true, List("Dundee Port", "Dundee")),
    //missing: Port("GRK", "title.faslane_docks", isGB = true, List("GRK (Greenock)")),
    Port("FNT", "title.finnart", isGB = true, List("GRK (Greenock)")),
    Port("FRB", "title.fraserburgh", isGB = true, Nil),
    //missing: Port("GRK", "title.furnace_docks", isGB = true, List("GRK (Greenock)")),
    //missing: Port("GRK", "title.garlieston_docks", isGB = true, List("GRK (Greenock)")),
    //missing: Port("AYR", "title.girvan_docks", isGB = true, List("AYR (Ayr)")),
    Port("GLW", "title.glasgow_docks", isGB = true, Nil),
    Port("GRG", "title.grangemouth", isGB = true, Nil),
    Port("GRK", "title.greenock_ocean_terminal", isGB = true, List("Greenock Port", "Greenock")),
    //missing: Port("HPT", "title.hound_point_terminal", isGB = true, Nil),
    Port("HST", "title.hunterston", isGB = true, List("GRK (Greenock)")),
    Port("IVG", "title.invergordon", isGB = true, List("IVG (Invergordon)")),
    Port("INK", "title.inverkeithing", isGB = true, List("GRG (Grangemouth)")),
    Port("INV", "title.inverness_port", isGB = true, List("Inverness")),
    //missing: Port("IRV", "title.irvine_port", isGB = true, Nil),
    //missing: Port("GRK", "title.islay_port", isGB = true, List("GRK (Greenock)")),
    //missing: Port("KKD", "title.kirkcaldy_port", isGB = true, Nil),
    //missing: Port("KWL", "title.kirkwall_port", isGB = true, Nil),
    Port("LEI", "title.leith", isGB = true, Nil),
    Port("LER", "title.lerwick", isGB = true, Nil),
    //missing: Port("INV", "title.lochaline_port", isGB = true, List("INV (Inverness)")),
    //missing: Port("INV", "title.lossiemouth_port", isGB = true, List("INV (Inverness)")),
    //missing: Port("FRB", "title.macduff_port", isGB = true, List("FRB (Fraserburgh)")),
    Port("MTH", "title.methil", isGB = true, List("GRG (Grangemouth)")),
    Port("MON", "title.montrose", isGB = true, Nil),
    //missing: Port("GRK", "title.oban_port", isGB = true, List("GRK (Greenock)")),
    //missing: Port("GRK", "title.palnackie_docks", isGB = true, List("GRK (Greenock)")),
    //missing: Port("DUN", "title.perth_docks", isGB = true, List("DUN (Dundee)")),
    Port("PHD", "title.peterhead", isGB = true, Nil),
    Port("ROS", "title.rosyth", isGB = true, List("Port of Rosyth")),
    //missing: Port("LER", "title.scalloway_docks", isGB = true, List("LER (Lerwick)")),
    //missing: Port("INV", "title.scrabster_docks", isGB = true, List("INV (Inverness)")),
    //missing: Port("STO", "title.stornoway_port", isGB = true, Nil),
    Port("CYN", "title.cairnryan", isGB = true, List("Stranraer", "GRK (Greenock)")),
    //missing: Port("KWL", "title.stromness_port", isGB = true, List("KWL (Kirkwall)")),
    //missing: Port("DUN", "title.tayport_docks", isGB = true, List("DUN (Dundee)")),
    //missing: Port("INV", "title.ullapool_docks", isGB = true, List("INV (Inverness)")),
    Port("WIC", "title.wick", isGB = true, Nil)
  )

  private val northernIrelandSeaports = List(
    Port("BEL", "title.belfast_docks", isGB = false, List("Belfast", "BEL")),
    //missing: Port("CLR", "title.coleraine_docks", isGB = false, Nil),
    Port("LAR", "title.larne", isGB = false, List("BEL (Belfast)")),
    Port("LDY", "title.derry_port", isGB = false, List("Foyle Port", "Londonderry Port", "Londonderry")),
    //missing: Port("CLR", "title.portrush", isGB = false, List("Portrush", "CLR (Coleraine)")),
    Port("WPT", "title.warrenpoint", isGB = false, Nil)
  )

  private val ukDesignatedAirports = List(
    Port("ABZ", "title.aberdeen_airport", isGB = true, Nil),
    Port("BFS", "title.belfast_international_airport", isGB = false, List("BFS")),
    Port("BQH", "title.biggin_hill_airport", isGB = true, List("London Biggin Hill Airport", "BQH")),
    Port("BHX", "title.birmingham_airport", isGB = true, List("BHX")),
    Port("BLK", "title.blackpool_airport", isGB = true, List("Blackpool International Airport", "BLK")),
    Port("BOH", "title.bournemouth_airport", isGB = true, List("Bournemouth (Hurn) Airport", "BOH")),
    Port("BRS", "title.bristol_airport", isGB = true, List("BRS")),
    Port("CBZ", "title.cambridge_city_airport", isGB = true, List("Cambridge Airport", "CBG")),
    Port("CWL", "title.cardiff_airport", isGB = true, List("Cardiff (Wales) Airport", "CWL")),
    Port("CVT", "title.coventry_airport", isGB = true, List("CVT")),
    Port("MME", "title.teeside_international_airport", isGB = true, List("Durham Tees Valley (Teesside) Airport", "MME")),
    Port("EMA", "title.east_midlands_airport", isGB = true, List("EMA")),
    Port("EDI", "title.edinburgh_airport", isGB = true, List("EDI")),
    Port("EXT", "title.exeter_airport", isGB = true, List("EXT")),
    Port("FAB", "title.farnborough_airport", isGB = true, List("FAB")),
    //missing: Port("FZO", "title.filton_airport", isGB = true, List("Filton Aerodrome (Bristol)", "FZO")),
    Port("GLA", "title.glasgow_airport", isGB = true, List("Glasgow International Airport", "GLA")),
    Port("HUY", "title.humberside_airport", isGB = true, List("Humberside International Airport", "HUY")),
    Port("LBA", "title.leeds_bradford_airport", isGB = true, List("Leeds Airport", "Bradford Airport", "LBA")),
    Port("LPL", "title.liverpool_john_lennon_airport", isGB = true, List("Liverpool Airport", "John Lennon Airport", "LPL")),
    Port("LYX", "title.lydd_airport", isGB = true, List("London Ashford (Lydd) Airport", "LYD")),
    Port("LCY", "title.london_city_airport", isGB = true, List("LCY")),
    Port("LGW", "title.gatwick_airport", isGB = true, List("London Gatwick Airport", "LGW")),
    Port("LHR", "title.heathrow_airport", isGB = true, List("London Heathrow Airport", "LHR")),
    Port("LTN", "title.luton_airport", isGB = true, List("London Luton Airport", "LTN")),
    Port("STN", "title.stansted_airport", isGB = true, List("London Stansted Airport", "LSA")),
    Port("MAN", "title.manchester_airport", isGB = true, List("MAN")),
    //missing: Port("MSE", "title.manston_airport", isGB = true, List("MSE")),
    Port("NCL", "title.newcastle_airport", isGB = true, List("Newcastle International Airport", "NCL")),
    Port("NQY", "title.newquay_airport", isGB = true, List("NQY")),
    Port("NWI", "title.norwich_airport", isGB = true, List("NWI")),
    Port("PLY", "title.plymouth", isGB = true, List("Plymouth Airport", "PLH")),
    Port("PIK", "title.glasgow_prestwick_airport", isGB = true, List("Prestwick Airport", "PIK")),
    Port("IOM", "title.isle_of_man_airport", isGB = true, List("Ronaldsway Airport (Isle of Man)", "RWY")),
    Port("ESH", "title.shoreham_airport", isGB = true, Nil),
    Port("SOU", "title.southampton_airport", isGB = true, List("Southampton (Eastleigh) Airport", "SOU")),
    Port("SEN", "title.southend_airport", isGB = true, Nil),
    Port("LSI", "title.sumburgh_airport", isGB = true, List("Sumburgh Airport (Shetland)", "LSI"))
  )

  private val ukNonDesignatedAirports = List(
    //missing: Port("BCC", "title.beccles_airport", isGB = true, Nil),
    Port("BHD", "title.george_best_belfast_city_airport", isGB = false, List("Belfast City Airport", "BHD")),
    //missing: Port("GLO", "title.gloucester_airport", isGB = true, Nil),
    //missing: Port("KEM", "title.kemble_airport", isGB = true, Nil),
    //missing: Port("LAS", "title.lasham_airport", isGB = true, Nil),
    //missing: Port("OAK", "title.oxford_airport", isGB = true, Nil),
    Port("DSA", "title.doncaster_sheffield_airport", isGB = true, List("Robin Hood Doncaster Sheffield", "DSA")),
    //missing: Port("SYW", "title.sywell_aerodrome", isGB = true, Nil),
    Port("STY", "title.stornoway_airport", isGB = true, List("STY")),
    //missing: Port("WAR", "title.warton_aerodrome", isGB = true, Nil),
    //missing: Port("WIK", "title.wick_airport", isGB = true, Nil),
    //missing: Port("YVL", "title.yeovil_aerodrome", isGB = true, Nil)
  )

  private val militaryAirfields = List(
    Port("BZN", "title.brize_norton", isGB = true, List("RAF Brize Norton", "BZZ")),
    //missing: Port("FFD", "title.fairford", isGB = true, Nil),
    //missing: Port("FLT", "title.feltwell", isGB = true, Nil),
    //missing: Port("LKH", "title.lakenheath", isGB = true, Nil),
    //missing: Port("LOS", "title.lossiemouth", isGB = true, Nil),
    //missing: Port("LYH", "title.lyneham", isGB = true, Nil),
    //missing: Port("MWH", "title.menwith_hill", isGB = true, Nil),
    //missing: Port("MDH", "title.mildenhall", isGB = true, Nil),
    Port("NHT", "title.northolt", isGB = true, List("RAF Northolt", "NHT")),
    //missing: Port("WAD", "title.waddington", isGB = true, Nil)
  )

  //not including UK rail stations
  private val ports = englandAndWalesSeaports ++ isleOfManSeaports ++ scotlandSeaports ++ northernIrelandSeaports ++ ukDesignatedAirports ++ ukNonDesignatedAirports ++ militaryAirfields
}
