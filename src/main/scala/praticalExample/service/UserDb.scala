package praticalExample.service

import cats.MonadError
import praticalExample.error.ExampleError
import praticalExample.service.model.{User, UserWithPassword}

trait UserDb[F[_]] {
  def getUser(id: Int)(implicit monadError: MonadError[F, Throwable]): F[User]
  def createUser(user: UserWithPassword)(
    implicit monadError: MonadError[F, Throwable]
  ): F[User]

  def getUserForAuth(id: Int)(
    implicit monadError: MonadError[F, Throwable]
  ): F[UserWithPassword]
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

    def createUser[F[_]](user: UserWithPassword)(
      implicit userDb: UserDb[F],
      monadError: MonadError[F, Throwable]
    ): F[User] = {
      userDb.createUser(user)
    }

    def getUserForAuth[F[_]](id: Int)(implicit userDb: UserDb[F],
                                      monadError: MonadError[F, Throwable]): F[UserWithPassword] =
      userDb.getUserForAuth(id)
  }
}
