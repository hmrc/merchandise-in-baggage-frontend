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

package uk.gov.hmrc.merchandiseinbaggage.auth

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.http.HeaderNames
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Result
import play.api.{Configuration, Environment, Mode}
import play.test.WithApplication
import uk.gov.hmrc.merchandiseinbaggage.BaseSpec

import java.io.File

class AuthRedirectsSpec extends BaseSpec with ScalaFutures {

  trait Dev {
    val mode: Mode = Mode.Dev
  }

  trait Prod {
    val mode: Mode = Mode.Prod
  }

  trait BaseUri {
    val strideService: String = "http://localhost:9041"
    val stridePath: String    = "/stride/sign-in"
  }

  trait Setup extends WithApplication with BaseUri {

    def mode: Mode

    def extraConfig: Map[String, Any] = Map()

    trait TestRedirects extends AuthRedirects {

      val env: Environment = Environment(new File("."), getClass.getClassLoader, mode)

      val config: Configuration = Configuration.from(
        Map(
          "appName"  -> "app",
          "run.mode" -> mode.toString
        ) ++ extraConfig
      )
    }

    object Redirect extends TestRedirects

    def validate(redirect: Result)(expectedLocation: String): Unit = {
      redirect.header.status                        shouldBe SEE_OTHER
      redirect.header.headers(HeaderNames.LOCATION) shouldBe expectedLocation
    }
  }

  "AuthRedirects" must {
    "redirecting with defaults from config" when {
      "redirect to stride auth in Dev without failureURL" in new Setup with Dev {
        validate(Redirect.toStrideLogin("/success"))(
          expectedLocation = s"$strideService$stridePath?successURL=%2Fsuccess&origin=app"
        )
      }

      "redirect to stride auth in Dev with failureURL" in new Setup with Dev {
        validate(Redirect.toStrideLogin("/success", Some("/failure")))(
          expectedLocation = s"$strideService$stridePath?successURL=%2Fsuccess&origin=app&failureURL=%2Ffailure"
        )
      }

      "redirect to stride auth in Prod without failureURL" in new Setup with Prod {
        validate(Redirect.toStrideLogin("/success"))(expectedLocation = s"$stridePath?successURL=%2Fsuccess&origin=app")
      }

      "redirect to stride auth in Prod with failureURL" in new Setup with Prod {
        validate(Redirect.toStrideLogin("/success", Some("/failure")))(
          expectedLocation = s"$stridePath?successURL=%2Fsuccess&origin=app&failureURL=%2Ffailure"
        )
      }

      "allow to override the host defaults" in new Setup with Dev {
        override def extraConfig: Map[String, String] =
          Map("Dev.external-url.stride-auth-frontend.host" -> "http://localhost:9099")

        validate(Redirect.toStrideLogin("/success"))(
          expectedLocation = s"http://localhost:9099$stridePath?successURL=%2Fsuccess&origin=app"
        )
      }

      "allow to override the origin default in configuration" in new Setup with Dev {

        override def extraConfig: Map[String, String] = Map("sosOrigin" -> "customOrigin")

        validate(Redirect.toStrideLogin("/success"))(
          expectedLocation = s"$strideService$stridePath?successURL=%2Fsuccess&origin=customOrigin"
        )
      }

      "allow to override the origin default in code" in new Setup with Dev {

        object CustomRedirect extends TestRedirects {
          override val origin = "customOrigin"
        }

        validate(CustomRedirect.toStrideLogin("/success"))(
          expectedLocation = s"$strideService$stridePath?successURL=%2Fsuccess&origin=customOrigin"
        )
      }
    }
  }
}
