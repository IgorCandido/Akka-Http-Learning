package util

import akka.http.scaladsl.server.directives.Credentials
import praticalExample.handler.adapter.AuthenticationAdapter
import praticalExample.service.model.User

import scala.concurrent.{ExecutionContext, Future}

object TestAuthentactionAdapter {
  implicit val testAuthentactionAdapter = new AuthenticationAdapter {
    override def authenticate(credentials: Credentials.Provided)(
      implicit executionContext: ExecutionContext
    ): Future[Option[User]] = ???
  }
}
