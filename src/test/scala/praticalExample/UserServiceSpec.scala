package praticalExample

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.MessageEntity
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{AsyncWordSpec, Matchers}
import praticalExample.error.{UserAlreadyExisted, UserDoesntExist}
import praticalExample.handler.UserHandler
import praticalExample.handler.adapter.UserAdapter
import praticalExample.handler.model.ErrorReply
import praticalExample.service.UserService
import praticalExample.service.model.{User, UserWithPassword}
import util.TestAuthentactionAdapter.testAuthentactionAdapter
import util.{TestData, TestIO}

import scala.concurrent.Future

class UserServiceSpec
    extends AsyncWordSpec
    with ScalatestRouteTest
    with Matchers {

  "UserService" should {
    "Return user when user exists" in {
      val userId = 1
      val user = User(id = userId, description = "Test")

      implicit val userDb = TestIO.userDb(user)

      val userService = new UserService[TestIO]

      val testIOUser = userService.getUser(userId)

      testIOUser.run(TestData()) should have('_2 (Right(user)))
    }

    "Create user successfully" in {
      val userId = 1
      val userWithPassword =
        UserWithPassword(id = userId, password = "1234", description = "Test")
      val user = User(id = userId, description = "Test")

      implicit val userDb = TestIO.userDb(user)

      val userService = new UserService[TestIO]

      val createdUserTestIO = userService.createUser(userWithPassword)

      createdUserTestIO.run(TestData()) should have(
        '_2 (Right(User(userId, "Test")))
      )
    }

    "Create user that already existed" in {
      val userId = 1
      val userWithPassword =
        UserWithPassword(id = userId, password = "1234", description = "Test")
      val user = User(id = userId, description = "Test")

      import TestIO.monadTestIO
      implicit val userDb = TestIO.userDb(
        monadTestIO.pure(user),
        monadTestIO.raiseError(UserAlreadyExisted),
        monadTestIO.pure(userWithPassword)
      )

      val userService = new UserService[TestIO]

      val createdUserTestIO = userService.createUser(userWithPassword)

      createdUserTestIO.run(TestData()) should have(
        '_2 (Left(UserAlreadyExisted))
      )
    }
  }

  "User Handler Routes" should {
    "Get a user get unknown error" in {
      implicit val userController = new UserAdapter {
        override def getUser(id: Int): Future[Either[Throwable, User]] =
          Future.successful(Left(new Exception("Test error")))

        override def createUser(
          user: UserWithPassword
        ): Future[Either[Throwable, User]] = ???
      }

      import praticalExample.handler.model.ExampleErrorJsonSupport._
      Get("/user/2") ~> UserHandler.route ~> check {
        responseAs[ErrorReply] should ===(
          ErrorReply(message = "Not able to serve your request", errorCode = 99)
        )
      }
    }

    "Get an existing user get success" in {
      implicit val userController = new UserAdapter {
        override def getUser(id: Int): Future[Either[Throwable, User]] =
          Future.successful(Right(User(id = id, description = "Tall")))

        override def createUser(
          user: UserWithPassword
        ): Future[Either[Throwable, User]] = ???
      }

      import UserHandler.jsonSupport._
      Get("/user/2") ~> UserHandler.route ~> check {
        responseAs[User] should ===(User(id = 2, description = "Tall"))
      }
    }

    "Get user that doesn't exist" in {
      implicit val userController = new UserAdapter {
        override def getUser(id: Int): Future[Either[Throwable, User]] =
          Future.successful(Left(UserDoesntExist(id)))

        override def createUser(
          user: UserWithPassword
        ): Future[Either[Throwable, User]] =
          ???
      }

      import praticalExample.handler.model.ExampleErrorJsonSupport._
      Get("/user/2") ~> UserHandler.route ~> check {
        responseAs[ErrorReply] should ===(
          ErrorReply(
            message = s"The user with id: ${2} for doesn't exist",
            errorCode = 2
          )
        )
      }
    }

    "Create user that doesn't exist yet" in {
      implicit val userController = new UserAdapter {
        override def getUser(id: Int): Future[Either[Throwable, User]] = ???

        override def createUser(
          user: UserWithPassword
        ): Future[Either[Throwable, User]] =
          Future.successful(Right(User(user.id, user.description)))
      }

      import praticalExample.handler.UserHandler.jsonSupport._
      val userWithPasswordEntity = Marshal[UserWithPassword](
        UserWithPassword(id = 2, password = "1234", description = "Tall")
      ).to[MessageEntity]

      userWithPasswordEntity.map { entity =>
        import praticalExample.handler.model.ExampleErrorJsonSupport._
        Post("/user").withEntity(entity) ~> UserHandler.route ~> check {
          responseAs[User] should ===(User(id = 2, description = "Tall"))
        }
      }
    }

    "Create user that already exist" in {
      implicit val userController = new UserAdapter {
        override def getUser(id: Int): Future[Either[Throwable, User]] = ???

        override def createUser(
          user: UserWithPassword
        ): Future[Either[Throwable, User]] =
          Future.successful(Left(UserAlreadyExisted))
      }

      import praticalExample.handler.UserHandler.jsonSupport._
      val userWithPasswordEntity = Marshal[UserWithPassword](
        UserWithPassword(id = 2, password = "1234", description = "Tall")
      ).to[MessageEntity]

      userWithPasswordEntity.map { entity =>
        import praticalExample.handler.model.ExampleErrorJsonSupport._
        Post("/user").withEntity(entity) ~> UserHandler.route ~> check {
          responseAs[ErrorReply] should ===(
            ErrorReply(
              message = "The user already exists on the system",
              errorCode = 1
            )
          )
        }
      }
    }
  }

}
