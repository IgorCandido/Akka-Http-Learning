package praticalExample.service

import cats.MonadError
import praticalExample.error.ExampleError
import praticalExample.service.model.User

trait UserDb[F[_]] {
  def getUser(id: Int)(
    implicit monadError: MonadError[F, Throwable]
  ): F[User]
  def createUser(user: User)(
    implicit monadError: MonadError[F, Throwable]
  ): F[User]
}

object UserDb {
  def apply[F[_]](implicit F: UserDb[F]) = F

  object dsl {
    def getUser[F[_]](id: Int)(
      implicit userDb: UserDb[F],
      monadError: MonadError[F, Throwable]
    ): F[User] = {
      userDb.getUser(id)
    }

    def createUser[F[_]](user: User)(
      implicit userDb: UserDb[F],
      monadError: MonadError[F, Throwable]
    ): F[User] = {
      userDb.createUser(user)
    }
  }
}
