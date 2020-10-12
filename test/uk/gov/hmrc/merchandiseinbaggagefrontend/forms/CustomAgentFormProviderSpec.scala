package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpec
import uk.gov.hmrc.merchandiseinbaggagefrontend.model.core.CustomsDeclares.{AgentDeclares, NoAgentDeclares}

class CustomAgentFormProviderSpec extends BaseSpec {

  val form = new CustomAgentFormProvider()

  "Bind CustomsDeclares data or return error" in {
    val bindData = Map("value" -> AgentDeclares.toString)
    val bindDataTwo = Map("value" -> NoAgentDeclares.toString)

    form().bind(bindData).data mustBe bindData
    form().bind(bindDataTwo).data mustBe bindDataTwo
    form().bind(Map[String, String]()).errors.head.message mustBe "customsDeclares.error.required"
  }
}
