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

import org.scalacheck.Arbitrary
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import uk.gov.hmrc.merchandiseinbaggage.auth.AuthRequest
import uk.gov.hmrc.merchandiseinbaggage.config.AppConfig
import uk.gov.hmrc.merchandiseinbaggage.controllers.{DeclarationGoodsRequest, DeclarationJourneyRequest}
import uk.gov.hmrc.merchandiseinbaggage.forms._
import uk.gov.hmrc.merchandiseinbaggage.model.api._
import uk.gov.hmrc.merchandiseinbaggage.model.api.calculation.CalculationResults
import uk.gov.hmrc.merchandiseinbaggage.model.core.{PurchaseDetailsInput, ThresholdAllowance}
import uk.gov.hmrc.merchandiseinbaggage.stubs.PayApiStub._
import uk.gov.hmrc.merchandiseinbaggage.views.html._
import uk.gov.hmrc.scalatestaccessibilitylinter.views.AutomaticAccessibilitySpec

class FrontendAccessibilitySpec extends AutomaticAccessibilitySpec {

  private val appConfig: AppConfig                 = app.injector.instanceOf[AppConfig]
  private val emailForm: Form[Email]               = EnterEmailForm.mandatoryForm
  private val optionEmailForm: Form[Option[Email]] = EnterEmailForm.optionalForm
  private val booleanForm: Form[Boolean]           = Form("value" -> boolean)
  private val stringForm: Form[String]             = Form("value" -> text)
  private val authRequest                          = AuthRequest(
    request = fakeRequest,
    credentials = None,
    isAssistedDigital = false
  )
  private val declarationJourneyRequest            = new DeclarationJourneyRequest(
    declarationJourney = completedDeclarationJourney,
    request = authRequest
  )
  private val declarationGoodsRequest              = new DeclarationGoodsRequest(
    declarationJourneyRequest = declarationJourneyRequest,
    goodsEntry = completedImportGoods
  )

  implicit val arbRequestHeader: Arbitrary[RequestHeader]                            = fixed(fakeRequest)
  implicit val arbHtml: Arbitrary[Html]                                              = fixed(Html(""))
  implicit val arbForm: Arbitrary[Form[_]]                                           = fixed(booleanForm)
  implicit val arbString: Arbitrary[String]                                          = fixed("http://something")
  implicit val arbFormString: Arbitrary[Form[String]]                                = fixed(stringForm)
  implicit val arbAppConfig: Arbitrary[AppConfig]                                    = fixed(appConfig)
  implicit val arbEnterEmailForm: Arbitrary[Form[Email]]                             = fixed(emailForm)
  implicit val arbEnterOptionEmailForm: Arbitrary[Form[Option[Email]]]               = fixed(optionEmailForm)
  implicit val arbDeclarationType: Arbitrary[DeclarationType]                        = fixed(DeclarationType.Import)
  implicit val arbGoodsDestination: Arbitrary[GoodsDestination]                      = fixed(GoodsDestinations.GreatBritain)
  implicit val arbDeclaration: Arbitrary[Declaration]                                = fixed(declaration)
  implicit val arbJourneyType: Arbitrary[JourneyType]                                = fixed(JourneyTypes.New)
  implicit val arbCheckYourAnswersImport: Arbitrary[CalculationResults]              = fixed(aCalculationResultsWithNoTax)
  implicit val arbCheckYourAnswersAmendExport: Arbitrary[Amendment]                  = fixed(aAmendment)
  implicit val arbCheckYourAnswersExport: Arbitrary[YesNo]                           = fixed(YesNo.Yes)
  implicit val arbJourneyDetailsEntry: Arbitrary[Form[JourneyDetailsEntry]]          = fixed(
    JourneyDetailsForm.form(DeclarationType.Import, journeyDate)
  )
  implicit val arbPreviousDeclarationDetails: Arbitrary[ThresholdAllowance]          = fixed(aThresholdAllowance)
  implicit val arbPurchaseDetailsExport: Arbitrary[Form[PurchaseDetailsInput]]       = fixed(PurchaseDetailsForm.form)
  implicit val arbTravellerDetails: Arbitrary[Form[Name]]                            = fixed(TravellerDetailsForm.form)
  implicit val arbAuthRequest: Arbitrary[AuthRequest[_]]                             = fixed(authRequest)
  implicit val arbDeclarationJourneyRequest: Arbitrary[DeclarationJourneyRequest[_]] = fixed(declarationJourneyRequest)
  implicit val arbDeclarationGoodsRequest: Arbitrary[DeclarationGoodsRequest[_]]     = fixed(declarationGoodsRequest)

  val viewPackageName = "uk.gov.hmrc.merchandiseinbaggage.views.html"

  override def layoutClasses: Seq[Class[Layout]] = Seq(classOf[Layout])

  override def renderViewByClass: PartialFunction[Any, Html] = {
    case agentDetailsView: AgentDetailsView                               => render(agentDetailsView)
    case cannotAccessPageView: CannotAccessPageView                       => render(cannotAccessPageView)
    case cannotUseServiceIrelandView: CannotUseServiceIrelandView         => render(cannotUseServiceIrelandView)
    case cannotUseServiceView: CannotUseServiceView                       => render(cannotUseServiceView)
    case checkYourAnswersAmendExportView: CheckYourAnswersAmendExportView => render(checkYourAnswersAmendExportView)
    case checkYourAnswersAmendImportView: CheckYourAnswersAmendImportView => render(checkYourAnswersAmendImportView)
    case checkYourAnswersExportView: CheckYourAnswersExportView           => render(checkYourAnswersExportView)
    case checkYourAnswersImportView: CheckYourAnswersImportView           => render(checkYourAnswersImportView)
    case customsAgentView: CustomsAgentView                               => render(customsAgentView)
    case declarationConfirmationView: DeclarationConfirmationView         => render(declarationConfirmationView)
    case declarationNotFoundView: DeclarationNotFoundView                 => render(declarationNotFoundView)
    case enterEmailView: EnterEmailView                                   => render(enterEmailView)
    case enterOptionalEmailView: EnterOptionalEmailView                   => render(enterOptionalEmailView)
    case eoriNumberView: EoriNumberView                                   => render(eoriNumberView)
    case errorTemplate: ErrorTemplate                                     => render(errorTemplate)
    case exciseAndRestrictedGoodsView: ExciseAndRestrictedGoodsView       => render(exciseAndRestrictedGoodsView)
    case goodsDestinationView: GoodsDestinationView                       => render(goodsDestinationView)
    case goodsInVehicleView: GoodsInVehicleView                           => render(goodsInVehicleView)
    case goodsOriginView: GoodsOriginView                                 => render(goodsOriginView)
    case goodsOverThresholdView: GoodsOverThresholdView                   => render(goodsOverThresholdView)
    case goodsRemovedView: GoodsRemovedView                               => render(goodsRemovedView)
    case goodsTypeView: GoodsTypeView                                     => render(goodsTypeView)
    case goodsVatRateView: GoodsVatRateView                               => render(goodsVatRateView)
    case importExportChoice: ImportExportChoice                           => render(importExportChoice)
    case journeyDetailsPage: JourneyDetailsPage                           => render(journeyDetailsPage)
    case newOrExistingView: NewOrExistingView                             => render(newOrExistingView)
    case noDeclarationNeededView: NoDeclarationNeededView                 => render(noDeclarationNeededView)
    case paymentCalculationView: PaymentCalculationView                   => render(paymentCalculationView)
    case previousDeclarationDetailsView: PreviousDeclarationDetailsView   => render(previousDeclarationDetailsView)
    case progressDeletedView: ProgressDeletedView                         => render(progressDeletedView)
    case purchaseDetailsExportView: PurchaseDetailsExportView             => render(purchaseDetailsExportView)
    case purchaseDetailsImportView: PurchaseDetailsImportView             => render(purchaseDetailsImportView)
    case removeGoodsView: RemoveGoodsView                                 => render(removeGoodsView)
    case retrieveDeclarationView: RetrieveDeclarationView                 => render(retrieveDeclarationView)
    case reviewGoodsView: ReviewGoodsView                                 => render(reviewGoodsView)
    case searchGoodsCountryView: SearchGoodsCountryView                   => render(searchGoodsCountryView)
    case serviceTimeoutView: ServiceTimeoutView                           => render(serviceTimeoutView)
    case testOnlyDeclarationJourneyPage: TestOnlyDeclarationJourneyPage   => render(testOnlyDeclarationJourneyPage)
    case travellerDetailsPage: TravellerDetailsPage                       => render(travellerDetailsPage)
    case valueWeightOfGoodsView: ValueWeightOfGoodsView                   => render(valueWeightOfGoodsView)
    case vehicleRegistrationNumberView: VehicleRegistrationNumberView     => render(vehicleRegistrationNumberView)
    case vehicleSizeView: VehicleSizeView                                 => render(vehicleSizeView)
  }

  runAccessibilityTests()
}
