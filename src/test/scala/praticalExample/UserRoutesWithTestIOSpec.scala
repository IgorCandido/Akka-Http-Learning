package praticalExample

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class UserRoutesWithTestIOSpec
    extends WordSpec
    with ScalatestRouteTest
    with Matchers {

  "User Flow" should {
    "Get User that doesn't exist" in {

    }
  }

}
