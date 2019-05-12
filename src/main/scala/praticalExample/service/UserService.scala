package praticalExample.service

import cats.Monad
import praticalExample.service.model.User

class UserService[F[_]: Monad: UserDb] {
  def getUser(id: Int): F[User] = {
    UserDb.dsl.getUser(id)
  }
}

object UserService {
  def apply[F[_]](implicit F: UserService[F]) = F

  object dsl {
    def getUser[F[_]](id: Int)(implicit userService: UserService[F]): F[User] =
      userService.getUser(id)
  }
}
