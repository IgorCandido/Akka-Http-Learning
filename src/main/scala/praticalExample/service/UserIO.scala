package praticalExample.service

import cats.MonadError
import cats.effect.IO
import praticalExample.service.model.User

object UserIO {
  implicit val userDbIO = new UserDb[IO] {
    override def getUser(
      id: Int
    )(implicit monadError: MonadError[IO, Throwable]): IO[User] = {
      monadError.pure(User(id = id, description = "Tall IO"))
    }

    override def createUser(
      user: User
    )(implicit monadError: MonadError[IO, Throwable]): IO[User] = {
      monadError.pure(user)
    }
  }

  implicit val userService = new UserService[IO]
}
