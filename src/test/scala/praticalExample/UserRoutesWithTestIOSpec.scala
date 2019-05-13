package praticalExample

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.MessageEntity
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import praticalExample.handler.UserHandler
import praticalExample.handler.model.ErrorReply
import praticalExample.service.model.{User, UserWithPassword}
import util.{TestData, UserTestIO}

class UserRoutesWithTestIOSpec
    extends WordSpec
    with ScalatestRouteTest
    with Matchers {

  "User Flow" should {
    "Get User that doesn't exist" in {
      import util.UserTestIO._

      import praticalExample.handler.model.ExampleErrorJsonSupport._
      Get("/user/2") ~> UserHandler.route ~> check {
        responseAs[ErrorReply] should ===(ErrorReply(
          message = s"The user with id: ${2} for doesn't exist",
          errorCode = 2
        ))
      }
    }

    "Get User that exists" in {
      val testData = TestData(Map((2 -> UserWithPassword(2, "1234", "Tall"))))
      implicit val authenticationAdapter = UserTestIO.authenticationAdapter(testData)
      implicit val userAdapter = UserTestIO.userController(testData)

      import UserHandler.jsonSupport._
      Get("/user/2") ~> UserHandler.route ~> check {
        responseAs[User] should ===(User(
          id= 2,
          description= "Tall"
        ))
      }
    }

    "Create user and then get that user" in {
      import UserTestIO._

      import praticalExample.handler.UserHandler.jsonSupport._
      val userWithPasswordEntity = Marshal[UserWithPassword](
        UserWithPassword(id = 2, password = "1234", description = "Tall")
      ).to[MessageEntity]

      userWithPasswordEntity.map { entity =>
        import praticalExample.handler.model.ExampleErrorJsonSupport._
        Post("/user").withEntity(entity) ~> UserHandler.route ~> check {
          responseAs[User] should ===(User(id = 2, description = "Tall"))
        }

        Get("/user/2") ~> UserHandler.route ~> check {
          responseAs[User] should ===(User(id = 2, description = "Tall"))
        }
      }
    }

    "Create user then get that user and access profile with auth" in {
      import UserTestIO._

      import praticalExample.handler.UserHandler.jsonSupport._
      val userWithPasswordEntity = Marshal[UserWithPassword](
        UserWithPassword(id = 2, password = "1234", description = "Tall")
      ).to[MessageEntity]

      userWithPasswordEntity.map { entity =>
        import praticalExample.handler.model.ExampleErrorJsonSupport._
        Post("/user").withEntity(entity) ~> UserHandler.route ~> check {
          responseAs[User] should ===(User(id = 2, description = "Tall"))
        }

        Get("/user/2") ~> UserHandler.route ~> check {
          responseAs[User] should ===(User(id = 2, description = "Tall"))
        }

        val validCredentials = BasicHttpCredentials("2", "1234")
        Get("/profile") ~> addCredentials(validCredentials) ~> UserHandler.route ~> check {
          responseAs[String] should ===("Tall")
        }
      }
    }
  }

}
