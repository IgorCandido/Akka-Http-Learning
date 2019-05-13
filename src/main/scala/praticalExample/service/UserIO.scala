package praticalExample.service

import cats.MonadError
import cats.effect.IO
import praticalExample.error.{UserAlreadyExisted, UserDoesntExist}
import praticalExample.handler.adapter.UserAdapter
import praticalExample.service.model.{User, UserWithPassword}
import cats.implicits._

import scala.concurrent.Future

object UserIO {
  implicit val userDbIO = new UserDb[IO] {
    var users: Map[Int, UserWithPassword] = Map.empty

    override def getUser(
      id: Int
    )(implicit monadError: MonadError[IO, Throwable]): IO[User] = {
      IO.async { cb =>
        {
          users.get(id).fold(cb(Left(UserDoesntExist(id)))) { user =>
            cb(Right(User(user.id, user.description)))
          }
        }
      }
    }

    override def createUser(
      user: UserWithPassword
    )(implicit monadError: MonadError[IO, Throwable]): IO[User] = {
      IO.async { cb =>
        users
          .get(user.id)
          .fold {
            users = users + (user.id -> user)
            cb(Either.right(User(user.id, user.password)))
          } { _ =>
            cb(Either.left(UserAlreadyExisted))
          }
      }
    }

    override def getUserForAuth(id: Int)(
      implicit monadError: MonadError[IO, Throwable]
    ): IO[UserWithPassword] = IO.async { cb =>
      users.get(id).fold(cb(Left(UserDoesntExist(id)))) { user =>
        cb(Right(user))
      }
    }
  }

  implicit val userService = new UserService[IO]

  implicit val userController = new UserAdapter {
    override def getUser(id: Int): Future[Either[Throwable, User]] =
      UserService.dsl.getUser[IO](id).attempt.unsafeToFuture()

    override def createUser(
      user: UserWithPassword
    ): Future[Either[Throwable, User]] =
      UserService.dsl.createUser[IO](user).attempt.unsafeToFuture()
  }
}
