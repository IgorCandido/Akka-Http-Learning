package praticalExample

import cats.MonadError
import org.scalatest.{Matchers, WordSpec}
import praticalExample.service.{UserDb, UserService}
import praticalExample.service.model.User
import util.TestIO
import org.scalatest.OptionValues._
import praticalExample.error.UserAlreadyExisted

class UserServiceSpec extends WordSpec with Matchers {

  "UserService" should {
    "Return user when user exists" in {
      val userId = 1
      val user = User(id = userId, description = "Test")

      import TestIO._
      implicit val userDb = TestIO.userDb(user)

      val userService = new UserService[TestIO]

      val testIOUser = userService.getUser(userId)

      testIOUser.value should contain (user)
    }

    "Create user successfully" in {
      val userId = 1
      val user = User(id = userId, description = "Test")

      import TestIO._
      implicit val userDb = TestIO.userDb(user)

      val userService = new UserService[TestIO]

      val createdUserTestIO = userService.createUser(user)

      createdUserTestIO.error shouldBe None
      createdUserTestIO.value should contain (User(userId, "Test"))
    }

    "Create user that already existed" in {
      val userId = 1
      val user = User(id = userId, description = "Test")

      implicit val userDb = new UserDb[TestIO] {
        override def getUser(id: Int)(
          implicit monadError: MonadError[TestIO, Throwable]
        ): TestIO[User] = monadError.pure(user)

        override def createUser(user: User)(
          implicit monadError: MonadError[TestIO, Throwable]
        ): TestIO[User] = monadError.raiseError(UserAlreadyExisted)
      }

      val userService = new UserService[TestIO]

      val createdUserTestIO = userService.createUser(user)

      createdUserTestIO.value shouldBe None
      createdUserTestIO.error should contain (UserAlreadyExisted)
    }
  }

}
