/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.model.core

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.{Enumerable, WithName}

sealed trait GoodsDestination

object GoodsDestination extends Enumerable.Implicits {

  case object NorthernIreland extends WithName("ni") with GoodsDestination
  case object EngScoWal extends WithName("gb") with GoodsDestination

  val values: Seq[GoodsDestination] = Seq(
    NorthernIreland,
    EngScoWal
  )

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map { value =>
    RadioItem(
      value = Some(value.toString),
      content = Text(messages(s"goodsDestination.${value.toString}")),
      checked = form("value").value.contains(value.toString)
    )
  }


  implicit val enumerable: Enumerable[GoodsDestination] =
    Enumerable(values.map(v => v.toString -> v): _*)
}