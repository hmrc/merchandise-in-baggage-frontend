/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.merchandiseinbaggagefrontend.service

import uk.gov.hmrc.merchandiseinbaggagefrontend.model.api.MibReference

import scala.util.Try

trait MibReferenceGenerator {

  protected val referenceFormat = """^X([A-Z])MB(\d{10})$"""
  protected val referenceFormatRegex = referenceFormat.r
  private def uniqueId: String = java.util.UUID.randomUUID().toString

  def mibReference: Try[MibReference] =
    for {
      bytes <- Try(java.nio.ByteBuffer.wrap(uniqueId.getBytes))
      truncate <- Try(bytes.asLongBuffer().get().toString.take(10))
    } yield MibReference(s"XXMB$truncate") //TODO prefix Letters hard coded
}
