package praticalExample.handler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.StandardRoute
import akka.http.scaladsl.server.directives.Credentials
import praticalExample.error.{UserAlreadyExisted, UserDoesntExist}
import praticalExample.handler.adapter.{AuthenticationAdapter, UserAdapter}
import praticalExample.handler.model.{ErrorReply, ErrorTypes}
import praticalExample.service.model.{User, UserWithPassword}
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContext, Future}

object UserHandler {
  object jsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val userFormat = jsonFormat2(User)
    implicit val userWithFormat = jsonFormat3(UserWithPassword)
  }

  private def transformToError(throwable: Throwable): ErrorReply = {
    throwable match {
      case UserAlreadyExisted =>
        ErrorReply(
          message = "The user already exists on the system",
          errorCode = ErrorTypes.UserAlreadyExists
        )
      case UserDoesntExist(userId) =>
        ErrorReply(
          message = s"The user with id: ${userId} for doesn't exist",
          errorCode = ErrorTypes.UserDoesntExist
        )
      case _: Throwable =>
        ErrorReply(
          message = "Not able to serve your request",
          errorCode = ErrorTypes.UnknownError
        )
    }
  }

  private def handleUserError(
    either: Either[Throwable, User]
  ): StandardRoute = {
    either match {
      case Left(error) =>
        import praticalExample.handler.model.ExampleErrorJsonSupport._
        complete(InternalServerError, transformToError(error))
      case Right(user) =>
        import jsonSupport._
        complete(user)
    }
  }

  import praticalExample.handler.adapter.AuthenticationAdapter.dsl._
  private def authentication(credential: Credentials)(
    implicit authenticationAdapter: AuthenticationAdapter,
    executionContext: ExecutionContext
  ): Future[Option[User]] = {
    credential match {
      case credential @ Credentials.Provided(id) =>
        authenticate(credential)
      case _ => Future.successful(None)
    }

  }

  def publicRoute = {
    import praticalExample.service.AuthenticationIO._
    import praticalExample.service.UserIO._
    route
  }

  import jsonSupport._
  import praticalExample.handler.adapter.UserAdapter.dsl._
  def route(implicit userController: UserAdapter,
            authenticationAdapter: AuthenticationAdapter) =
    get {
      path("user" / IntNumber) { userId =>
        onSuccess(getUser(userId)) { userOrError =>
          handleUserError(userOrError)
        }
      }
    } ~
      post {
        path("user") {
          entity(as[UserWithPassword]) { user =>
            onSuccess(createUser(user)) { userOrError =>
              handleUserError(userOrError)
            }
          }
        }
      } ~
      get {
        path("profile") {
          extractExecutionContext { implicit executionContext =>
            authenticateBasicAsync("pratical Example", authentication) { user =>
              complete(user.description)
            }
          }
        }
      }
}
