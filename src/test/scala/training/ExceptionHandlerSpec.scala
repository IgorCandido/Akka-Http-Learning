package training

import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class ExceptionHandlerSpec extends WordSpec with ScalatestRouteTest with Matchers {

  "Exception handling" should {
    "When calling hi will return Hi there" in {
      Get("/hi") ~> ExceptionHandling.route ~> check{
        handled should ===(true)
        responseAs[String] should===("Hi there")
      }
    }

    "When calling boom will return an error with the url" in {
      import ExceptionHandling.exceptionHandler
      Get("/boom") ~> ExceptionHandling.route ~> check{
        handled should ===(true)
        responseAs[String] should ===("Failed calling url: http://example.com/boom")
      }
    }
  }

}
