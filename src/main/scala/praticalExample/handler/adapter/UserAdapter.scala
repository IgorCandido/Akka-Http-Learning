package praticalExample.handler.adapter
import praticalExample.service.model.{User, UserWithPassword}

import scala.concurrent.Future

trait UserAdapter {
  def getUser(id: Int): Future[Either[Throwable, User]]
  def createUser(user: UserWithPassword): Future[Either[Throwable, User]]
}

object UserAdapter {
  object dsl {
    def getUser(id: Int)(
      implicit userController: UserAdapter
    ): Future[Either[Throwable, User]] =
      userController.getUser(id)

    def createUser(user: UserWithPassword)(
      implicit userController: UserAdapter
    ): Future[Either[Throwable, User]] =
      userController.createUser(user)
  }
}
