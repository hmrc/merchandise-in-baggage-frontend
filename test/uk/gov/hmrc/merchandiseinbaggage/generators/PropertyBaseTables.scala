package uk.gov.hmrc.merchandiseinbaggage.generators

import org.scalatest.prop.TableFor1
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}

trait PropertyBaseTables extends ScalaCheckPropertyChecks {

  val declarationTypes: TableFor1[DeclarationType] = Table("declarationType", Import, Export)

  val traderYesOrNoAnswer = Table(
    ("answer", "trader or agent"),
    (Yes, "agent"),
    (No, "trader")
  )
}
