package praticalExample.handler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import praticalExample.service.UserService
import praticalExample.service.model.User
import spray.json.DefaultJsonProtocol
import praticalExample.service.UserIO._

object UserHandler {
  object jsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val userFormat = jsonFormat2(User)
  }

  import jsonSupport._
  import UserService.dsl._
  def route = get {
    path("user" / IntNumber) { userId =>
      onSuccess(getUser[IO](userId).unsafeToFuture()) { user =>
        complete(user)
      }
    }
  }
}
