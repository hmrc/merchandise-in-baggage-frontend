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

package uk.gov.hmrc.merchandiseinbaggage

import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.Html
import config.AppConfig
import forms._
import model.api.{DeclarationType, Email}
import views.html._
import uk.gov.hmrc.scalatestaccessibilitylinter.views.AutomaticAccessibilitySpec

class FrontendAccessibilitySpec
  extends AutomaticAccessibilitySpec {

  private val appConfig: AppConfig                                 = app.injector.instanceOf[AppConfig]
  private val emailForm: Form[Email] = EnterEmailForm.mandatoryForm

  implicit val arbHtml: Arbitrary[Html]                             = fixed(Html(""))
  implicit val arbAppConfig: Arbitrary[AppConfig]                   = fixed(appConfig)
  implicit val arbEnterEmailForm: Arbitrary[Form[Email]] = fixed(emailForm)
  implicit val arbDeclarationType: Arbitrary[DeclarationType] = fixed(DeclarationType.Import)

  val viewPackageName = "uk.gov.hmrc.merchandiseinbaggage.views.html"

  override def layoutClasses: Seq[Class[Layout]] = Seq(classOf[Layout])

  override def renderViewByClass: PartialFunction[Any, Html] = {
    //case agentDetailsView: AgentDetailsView => render(agentDetailsView)
    case cannotAccessPageView: CannotAccessPageView => render(cannotAccessPageView)
    //case cannotUseServiceIrelandView: CannotUseServiceIrelandView => render(cannotUseServiceIrelandView)
    //case cannotUseServiceView: CannotUseServiceView => render(cannotUseServiceView)
    //case checkYourAnswersAmendExportView: CheckYourAnswersAmendExportView => render(checkYourAnswersAmendExportView)
    //case checkYourAnswersAmendImportView: CheckYourAnswersAmendImportView => render(checkYourAnswersAmendImportView)
    //case checkYourAnswersExportView: CheckYourAnswersExportView => render(checkYourAnswersExportView)
    //case checkYourAnswersImportView: CheckYourAnswersImportView => render(checkYourAnswersImportView)
    //case customsAgentView: CustomsAgentView => render(customsAgentView)
    //case declarationNotFoundView: DeclarationNotFoundView => render(declarationNotFoundView)
    //case declarationNotFoundView: DeclarationNotFoundView => render(declarationNotFoundView)
    //case enterEmailView: EnterEmailView => render(enterEmailView)
    //case enterOptionalEmailView: EnterOptionalEmailView => render(enterOptionalEmailView)
    //case eoriNumberView: EoriNumberView => render(eoriNumberView)
    //case errorTemplate: ErrorTemplate                                           => render(errorTemplate)
    //case exciseAndRestrictedGoodsView: ExciseAndRestrictedGoodsView => render(exciseAndRestrictedGoodsView)
    //case goodsDestinationView: GoodsDestinationView => render(goodsDestinationView)
//    case goodsInVehicleView: GoodsInVehicleView => render(goodsInVehicleView)
//    case goodsOriginView: GoodsOriginView => render(goodsOriginView)
//    case goodsOverThresholdView: GoodsOverThresholdView => render(goodsOverThresholdView)
//    case goodsRemovedView: GoodsRemovedView => render(goodsRemovedView)
//    case goodsTypeView: GoodsTypeView => render(goodsTypeView)
//    case goodsVatRateView: GoodsVatRateView => render(goodsVatRateView)
//    case importExportChoice: ImportExportChoice => render(importExportChoice)
//    case journeyDetailsPage: JourneyDetailsPage => render(journeyDetailsPage)
//    case newOrExistingView: NewOrExistingView => render(newOrExistingView)
//    case noDeclarationNeededView: NoDeclarationNeededView => render(noDeclarationNeededView)
//    case paymentCalculationView: PaymentCalculationView => render(paymentCalculationView)
//    case previousDeclarationDetailsView: PreviousDeclarationDetailsView => render(previousDeclarationDetailsView)
//    case progressDeletedView: ProgressDeletedView => render(progressDeletedView)
//    case purchaseDetailsExportView: PurchaseDetailsExportView => render(purchaseDetailsExportView)
//    case purchaseDetailsImportView: PurchaseDetailsImportView => render(purchaseDetailsImportView)
//    case removeGoodsView: RemoveGoodsView => render(removeGoodsView)
//    case retrieveDeclarationView: RetrieveDeclarationView => render(retrieveDeclarationView)
//    case reviewGoodsView: ReviewGoodsView => render(reviewGoodsView)
//    case searchGoodsCountryView: SearchGoodsCountryView => render(searchGoodsCountryView)
//    case travellerDetailsPage: TravellerDetailsPage => render(travellerDetailsPage)
//    case valueWeightOfGoodsView: ValueWeightOfGoodsView => render(valueWeightOfGoodsView)
//    case vehicleRegistrationNumberView: VehicleRegistrationNumberView => render(vehicleRegistrationNumberView)
//    case vehicleSizeView: VehicleSizeView => render(vehicleSizeView)

  }

  runAccessibilityTests()
}