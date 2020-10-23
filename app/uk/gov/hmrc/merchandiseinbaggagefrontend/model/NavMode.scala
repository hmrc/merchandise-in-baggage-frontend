package uk.gov.hmrc.merchandiseinbaggagefrontend.model

import play.api.mvc.JavascriptLiteral

sealed trait NavMode

case object NormalMode extends NavMode
case object ReviewGoodsMode extends NavMode

object NavMode {
  implicit val jsLiteral: JavascriptLiteral[NavMode] = new JavascriptLiteral[NavMode] {
    override def to(value: NavMode): String = value match {
      case NormalMode => "NormalMode"
      case ReviewGoodsMode  => "ReviewGoodsMode"
    }
  }
}
