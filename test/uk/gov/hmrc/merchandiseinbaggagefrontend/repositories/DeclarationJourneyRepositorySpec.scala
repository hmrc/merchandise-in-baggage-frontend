/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.merchandiseinbaggagefrontend.repositories

import uk.gov.hmrc.merchandiseinbaggagefrontend.{BaseSpecWithApplication, CoreTestData}

class DeclarationJourneyRepositorySpec extends BaseSpecWithApplication with CoreTestData{
  "persist and find declaration object" in {
    val inserted = declarationJourneyRepository.insert(completedDeclarationJourney).futureValue

    inserted mustBe completedDeclarationJourney

    declarationJourneyRepository.findBySessionId(completedDeclarationJourney.sessionId).futureValue mustBe Some(completedDeclarationJourney)
  }
}
