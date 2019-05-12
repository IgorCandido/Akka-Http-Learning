package praticalExample.service

import cats.effect.IO
import praticalExample.service.model.User

object UserIO {
  implicit val userDbIO = new UserDb[IO] {
    override def getUser(id: Int): IO[User] =
      {
        IO.pure(User(id= id, description = "Tall IO"))
      }
  }

  implicit val userService = new UserService[IO]
}
