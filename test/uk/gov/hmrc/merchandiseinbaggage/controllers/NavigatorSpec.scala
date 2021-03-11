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

package uk.gov.hmrc.merchandiseinbaggage.controllers

import play.api.mvc.Call
import uk.gov.hmrc.merchandiseinbaggage.controllers.routes._
import uk.gov.hmrc.merchandiseinbaggage.generators.PropertyBaseTables
import uk.gov.hmrc.merchandiseinbaggage.model.api.DeclarationType.{Export, Import}
import uk.gov.hmrc.merchandiseinbaggage.model.api.GoodsDestinations.{GreatBritain, NorthernIreland}
import uk.gov.hmrc.merchandiseinbaggage.model.api.JourneyTypes.{Amend, New}
import uk.gov.hmrc.merchandiseinbaggage.model.api.YesNo.{No, Yes}
import uk.gov.hmrc.merchandiseinbaggage.model.api.{DeclarationType, JourneyType}

class NavigatorSpec extends DeclarationJourneyControllerSpec with PropertyBaseTables {

  forAll(declarationTypesTable) { importOrExport: DeclarationType =>
    forAll(journeyTypesTable) { newOrAmend: JourneyType =>
      s"On ${ExciseAndRestrictedGoodsController.onPageLoad().url}" must {
        s"redirect to ${CannotUseServiceController.onPageLoad().url} if submit with Yes for $importOrExport and $newOrAmend" in new Navigator {
          val result: Call = nextPage(RequestWithIndex(ExciseAndRestrictedGoodsController.onPageLoad().url, Yes, newOrAmend, 1))

          result mustBe CannotUseServiceController.onPageLoad()
        }

        if (newOrAmend == Amend) {
          s"redirect to ${GoodsTypeQuantityController.onPageLoad(1).url} for $newOrAmend on submit for $importOrExport" in new Navigator {
            val result: Call = nextPage(RequestWithIndex(ExciseAndRestrictedGoodsController.onPageLoad().url, No, newOrAmend, 1))

            result mustBe GoodsTypeQuantityController.onPageLoad(1)
          }
        }

        if (newOrAmend == New) {
          s"redirect to ${GoodsTypeQuantityController.onPageLoad(1).url} for $newOrAmend on submit for $importOrExport" in new Navigator {
            val result: Call = nextPage(RequestWithIndex(ExciseAndRestrictedGoodsController.onPageLoad().url, No, newOrAmend, 1))

            result mustBe ValueWeightOfGoodsController.onPageLoad()
          }
        }
      }
      s"from ${EnterEmailController.onPageLoad().url} navigates to ${JourneyDetailsController.onPageLoad().url} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPass(EnterEmailController.onPageLoad().url))

        result mustBe JourneyDetailsController.onPageLoad()
      }

      s"from ${EoriNumberController.onPageLoad().url} navigates to ${TravellerDetailsController.onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPass(EoriNumberController.onPageLoad().url))

        result mustBe TravellerDetailsController.onPageLoad()
      }

      s"from ${GoodsDestinationController.onPageLoad().url} navigates to ${ExciseAndRestrictedGoodsController
        .onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestWithAnswer(GoodsDestinationController.onPageLoad().url, GreatBritain))

        result mustBe ExciseAndRestrictedGoodsController.onPageLoad()
      }

      s"from ${GoodsDestinationController.onPageLoad().url} navigates to ${CannotUseServiceIrelandController
        .onPageLoad()} if NorthernIreland for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestWithAnswer(GoodsDestinationController.onPageLoad().url, NorthernIreland))

        result mustBe CannotUseServiceIrelandController.onPageLoad()
      }

      s"from ${GoodsInVehicleController.onPageLoad().url} navigates to ${VehicleSizeController
        .onPageLoad()} if Yes for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestWithAnswer(GoodsInVehicleController.onPageLoad().url, Yes))

        result mustBe VehicleSizeController.onPageLoad()
      }

      s"from ${GoodsInVehicleController.onPageLoad().url} navigates to ${CheckYourAnswersController
        .onPageLoad()} if No for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestWithAnswer(GoodsInVehicleController.onPageLoad().url, No))

        result mustBe CheckYourAnswersController.onPageLoad()
      }

      s"from ${GoodsOriginController.onPageLoad(1).url} navigates to ${PurchaseDetailsController
        .onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPassWithIndex(GoodsOriginController.onPageLoad(1).url, 1))

        result mustBe PurchaseDetailsController.onPageLoad(1)
      }

      if(importOrExport == Import) {
        s"from ${GoodsTypeQuantityController.onPageLoad(1).url} navigates to ${GoodsVatRateController
          .onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
          val result: Call = nextPage(RequestWithDeclarationType(GoodsTypeQuantityController.onPageLoad(1).url, importOrExport, 1))

          result mustBe GoodsVatRateController.onPageLoad(1)
        }
      }

      if (importOrExport == Export) {
        s"from ${GoodsTypeQuantityController.onPageLoad(1).url} navigates to ${SearchGoodsCountryController
          .onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
          val result: Call = nextPage(RequestWithDeclarationType(GoodsTypeQuantityController.onPageLoad(1).url, importOrExport, 1))

          result mustBe SearchGoodsCountryController.onPageLoad(1)
        }
      }
      s"from ${GoodsVatRateController.onPageLoad(1).url} navigates to ${SearchGoodsCountryController
        .onPageLoad(1)} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPassWithIndex(GoodsVatRateController.onPageLoad(1).url, 1))

        result mustBe SearchGoodsCountryController.onPageLoad(1)
      }

      s"from ${JourneyDetailsController.onPageLoad().url} navigates to ${GoodsInVehicleController
        .onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
        val result: Call = nextPage(RequestByPass(JourneyDetailsController.onPageLoad().url))

        result mustBe GoodsInVehicleController.onPageLoad()
      }

      if(newOrAmend == New) {
        s"from ${NewOrExistingController.onPageLoad().url} navigates to ${GoodsDestinationController
          .onPageLoad()} for $newOrAmend & $importOrExport" in new Navigator {
          val result: Call = nextPage(RequestWithAnswer(NewOrExistingController.onPageLoad().url, newOrAmend))

          result mustBe GoodsDestinationController.onPageLoad()
        }
      }
    }
  }
}
