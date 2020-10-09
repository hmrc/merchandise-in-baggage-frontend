package uk.gov.hmrc.merchandiseinbaggagefrontend.forms

import play.api.data.FormError
import uk.gov.hmrc.merchandiseinbaggagefrontend.BaseSpec

class CheckYourAnswersFormProviderSpec extends BaseSpec {


  "bind data to the form" in {
    val provider = new CheckYourAnswersFormProvider()

    provider().bind(Map("taxDue" -> "30.12")).value mustBe Some(Answers(30.12))
   }

  "return error if incorrect" in {
    val provider = new CheckYourAnswersFormProvider()

    provider().bind(Map[String, String]()).errors mustBe List(FormError("taxDue", List("error.required"),List()))
   }

}
