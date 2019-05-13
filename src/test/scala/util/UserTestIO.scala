package util

import akka.http.scaladsl.server.directives.Credentials
import cats.MonadError
import cats.implicits._
import praticalExample.error.{UserAlreadyExisted, UserDoesntExist}
import praticalExample.handler.adapter.{AuthenticationAdapter, UserAdapter}
import praticalExample.service.{AuthenticatorService, UserDb, UserService}
import praticalExample.service.model.{User, UserWithPassword}

import scala.concurrent.{ExecutionContext, Future}

object UserTestIO {
  implicit val userDbTestIO = new UserDb[TestIO] {
    override def getUser(id: Int)(
      implicit monadError: MonadError[TestIO, Throwable]
    ): TestIO[User] = TestIO { testData =>
      testData.users
        .get(id)
        .fold((testData, Either.left[Throwable, User](UserDoesntExist(id)))) {
          user =>
            (testData, Right(User(user.id, user.description)))
        }
    }

    override def createUser(user: UserWithPassword)(
      implicit monadError: MonadError[TestIO, Throwable]
    ): TestIO[User] = TestIO { testData =>
      testData.users.get(user.id).fold {
        testData.users = testData.users + (user.id -> user)
        (testData, Either.right[Throwable, User](User(user.id, user.description)))
      }{_ => (testData, Either.left(UserAlreadyExisted))}
    }

    override def getUserForAuth(id: Int)(
      implicit monadError: MonadError[TestIO, Throwable]
    ): TestIO[UserWithPassword] = TestIO{
      testData => testData.users
        .get(id)
        .fold((testData, Either.left[Throwable, UserWithPassword](UserDoesntExist(id)))) {
          user =>
            (testData, Right(user))
        }
    }
  }

  implicit val userServiceTestIO = new UserService[TestIO]

  implicit val userControllerImplicit = userController(TestData())

  def userController(testData: TestData) =
    new UserAdapter {
      override def getUser(id: Int): Future[Either[Throwable, User]] =
        Future.successful(UserService.dsl.getUser[TestIO](id).run(testData)._2)

      override def createUser(
                               user: UserWithPassword
                             ): Future[Either[Throwable, User]] =
        Future.successful(UserService.dsl.createUser[TestIO](user).run(testData)._2)
    }

  implicit val authenticationIO = new AuthenticatorService[TestIO] {
    import UserDb.dsl._
    override def authenticate(id: Int): TestIO[UserWithPassword] = {
      getUserForAuth(id)
    }
  }

  implicit val authenticationAdapterImplicit = authenticationAdapter(TestData())

  def authenticationAdapter(testData: TestData) =  new AuthenticationAdapter {
    import praticalExample.service.AuthenticatorService.dsl._
    override def authenticate(
                               credentials: Credentials.Provided
                             )(implicit executionContext: ExecutionContext): Future[Option[User]] = {
      var id: Int = 0
      try {
        id = credentials.identifier.toInt
      } catch {
        case _: Throwable => Future.successful(None)
      }

      auth(id)
        .map { userWithPassword =>
          if (credentials.verify(userWithPassword.password))
            User(userWithPassword.id, userWithPassword.description).some
          else None
        }.run(testData)._2 match {
        case Left(_)       => Future.successful(None)
        case Right(result) => Future.successful(result)
      }
    }
  }
}
