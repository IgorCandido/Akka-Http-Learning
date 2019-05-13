package praticalExample.handler.adapter

import akka.http.scaladsl.server.directives.Credentials
import praticalExample.service.model.User

import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationAdapter {
  def authenticate(credentials: Credentials.Provided)(
    implicit executionContext: ExecutionContext
  ): Future[Option[User]]
}

object AuthenticationAdapter {
  object dsl {
    def authenticate(credentials: Credentials.Provided)(
      implicit authenticationAdapter: AuthenticationAdapter,
      executionContext: ExecutionContext
    ): Future[Option[User]] =
      authenticationAdapter.authenticate(credentials)
  }
}
