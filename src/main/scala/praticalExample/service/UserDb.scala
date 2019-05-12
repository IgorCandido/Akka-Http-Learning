package praticalExample.service

import praticalExample.service.model.User

trait UserDb[F[_]] {
  def getUser(id: Int): F[User]
}

object UserDb{
  def apply[F[_]](implicit F: UserDb[F]) = F

  object dsl {
    def getUser[F[_]](id: Int)(implicit userDb: UserDb[F]): F[User] ={
      userDb.getUser(id)
    }
  }
}
