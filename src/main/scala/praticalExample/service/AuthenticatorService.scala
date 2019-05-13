package praticalExample.service

import praticalExample.service.model.{UserWithPassword}

trait AuthenticatorService[F[_]] {
  def authenticate(id: Int): F[UserWithPassword]
}

object AuthenticatorService {
  def apply[F[_]](
    implicit F: AuthenticatorService[F]
  ): AuthenticatorService[F] = F

  object dsl {
    def auth[F[_]: AuthenticatorService](id: Int): F[UserWithPassword] =
      AuthenticatorService[F]
        .authenticate(id = id)

  }
}
