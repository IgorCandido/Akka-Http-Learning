package training

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class RejectionHandlingSpec extends WordSpec with ScalatestRouteTest with Matchers {

  "Rejection test" should {
    "return hi when calling hi" in {
      Get("/hi") ~> RejectionHandling.route ~> check{
        handled should ===(true)
        responseAs[String] ===("True hi")
      }
    }

    "return goodbye when calling goodbye" in {
      Get("/goodbye") ~> RejectionHandling.route ~> check{
        handled should ===(true)
        responseAs[String] ===("Goodbye hello")
      }
    }

    "return wrong url when unhandled path" in {
      import RejectionHandling.rejectionHandler
      Get("/asaf") ~> Route.seal(RejectionHandling.route) ~> check{
        handled should ===(true)
        responseAs[String] ===("You did not enter the right path on your thing portal")
      }
    }
  }
}
