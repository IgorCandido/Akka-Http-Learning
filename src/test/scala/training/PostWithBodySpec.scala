package training

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.MessageEntity
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{AsyncWordSpec, Matchers}
import training.PostWithBody.{InfoReply, RequestInfo}

class PostWithBodySpec extends AsyncWordSpec with ScalatestRouteTest with Matchers {

  "Server handles posts" should {
    "Return info when info is requested" in {
      import PostWithBody.jsonSupport._
      val requestInfo = RequestInfo(name = "test", description = "Tall")
      val requestInfoMarshelled = Marshal(requestInfo).to[MessageEntity]

      requestInfoMarshelled.map{ marshelled =>
        Post("/infoEnquire").withEntity(marshelled) ~> PostWithBody.route ~> check{
          responseAs[InfoReply] should have(
            'name ("test"),
            'description ("Tall")
          )
        }
      }
    }
  }

}
