package praticalExample.service

import akka.http.scaladsl.server.directives.Credentials
import cats.effect.IO
import praticalExample.handler.adapter.AuthenticationAdapter
import praticalExample.service.model.{User, UserWithPassword}
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

object AuthenticationIO {

  import UserIO.userDbIO
  implicit val authenticationIO = new AuthenticatorService[IO] {
    import UserDb.dsl._
    override def authenticate(id: Int): IO[UserWithPassword] = {
      getUserForAuth(id)
    }
  }

  implicit val authenticationAdapter = new AuthenticationAdapter {
    import AuthenticatorService.dsl._
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
        }
        .attempt
        .unsafeToFuture()
        .map {
          case Left(_)       => None
          case Right(result) => result
        }
    }
  }
}
