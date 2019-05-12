package praticalExample.service

import cats.{Monad, MonadError}
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import praticalExample.service.model.User

class UserService[F[_]: Monad: UserDb](implicit me: MonadError[F, Throwable]) {
  def getUser(id: Int): F[User] = {
    UserDb.dsl.getUser(id)
  }

  def createUser(user: User): F[User] = {
    UserDb.dsl.createUser(user)
  }
}

object UserService {
  def apply[F[_]](implicit F: UserService[F]) = F

  object dsl {
    def getUser[F[_]](id: Int)(implicit userService: UserService[F]): F[User] =
      userService.getUser(id)

    def createUser[F[_]](
      user: User
    )(implicit userService: UserService[F]): F[User] =
      userService.createUser(user)
  }
}
