package uk.gov.hmrc.merchandiseinbaggage.utils

import scala.concurrent.Future

object Utils {
  implicit class FutureOps[T](obj: T) {
    def asFuture: Future[T] = Future.successful(obj)
  }
}
