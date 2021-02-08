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

package uk.gov.hmrc.merchandiseinbaggage.pagespecs

import com.softwaremill.quicklens._
import uk.gov.hmrc.merchandiseinbaggage.CoreTestData
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResult
import uk.gov.hmrc.merchandiseinbaggage.model.core.DeclarationJourney
import uk.gov.hmrc.merchandiseinbaggage.stubs.MibBackendStub._
import uk.gov.hmrc.merchandiseinbaggage.utils.DataModelEnriched._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait TaxCalculation extends CoreTestData {
  this: BasePageSpec[_] =>

  def givenADeclarationWithTaxDue(
    declarationJourney: DeclarationJourney,
    overThreshold: Option[Int] = Some(0)): Future[PaymentCalculations] = {

    val resultOne =
      CalculationResult(aImportGoods, AmountInPence(7834), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val resultTwo = CalculationResult(aImportGoods, AmountInPence(7834), AmountInPence(0), AmountInPence(1567), Some(aConversionRatePeriod))
    val calculations: Seq[CalculationResult] = overThreshold.fold(List(resultOne, resultTwo))(over =>
      List(resultOne, resultTwo).map(_.modify(_.gbpAmount).using(inPence => AmountInPence(inPence.value + over))))

    val goods: Seq[ImportGoods] = declarationJourney.goodsEntries.declarationGoodsIfComplete.get.importGoods

    givenADeclarationJourney(declarationJourney)
    givenAPaymentCalculations(goods.map(_.calculationRequest), calculations)

    Future(PaymentCalculations(calculations.map(res => PaymentCalculation(res.goods, res))))
  }
}
