import akka.http.scaladsl.model.{ContentTypes, MediaRange, MediaTypes}
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.server.UnacceptedResponseContentTypeRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class StreamingExamplesSpec
    extends WordSpec
    with ScalatestRouteTest
    with Matchers {


  val AcceptJson = Accept(MediaRange(MediaTypes.`application/json`))
  val AcceptXml = Accept(MediaRange(MediaTypes.`text/xml`))

  "The streaming example" should {
    import example.StreamingExamples._
    "return tweets when asking json" in {
      Get("/tweets").withHeaders(AcceptJson) ~> route ~> check {
        responseAs[String] shouldEqual
          """[""" +
            """{"txt":"One","uid":1},""" +
            """{"txt":"Two","uid":2},""" +
            """{"txt":"Three","uid":3},""" +
            """{"txt":"Four","uid":4},""" +
            """{"txt":"Five","uid":5}""" +
            """]"""
      }
    }

    "return content type unacceptable when asking xml" in {
      Get("/tweets").withHeaders(AcceptXml) ~> route ~> check {
        handled should ===(false)
        rejection should ===(
          UnacceptedResponseContentTypeRejection(
            Set(ContentTypes.`application/json`)
          )
        )
      }
    }
  }

  "The streaming example for new lines" should{
    import example.StreamingExamplesWithLineByLineFraming._
    "return tweets with newlines when asking json" in {
      Get("/tweets").withHeaders(AcceptJson) ~> route ~> check {
        responseAs[String] shouldEqual
            """{"txt":"One","uid":1}""" + "\n" +
            """{"txt":"Two","uid":2}""" + "\n"  +
            """{"txt":"Three","uid":3}""" + "\n"  +
            """{"txt":"Four","uid":4}""" + "\n"  +
            """{"txt":"Five","uid":5}""" + "\n"
      }
    }

    "return content type unacceptable when asking xml" in {
      Get("/tweets").withHeaders(AcceptXml) ~> route ~> check {
        handled should ===(false)
        rejection should ===(
          UnacceptedResponseContentTypeRejection(
            Set(ContentTypes.`application/json`)
          )
        )
      }
    }
  }

}
