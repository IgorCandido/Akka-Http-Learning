package util

import cats.{Monad, MonadError}
import cats.implicits._
import praticalExample.error.{UserAlreadyExisted, UserDoesntExist}
import praticalExample.service.UserDb
import praticalExample.service.model.{User, UserWithPassword}

case class TestData(var users: Map[Int, UserWithPassword] = Map.empty)

case class TestIO[A](run: TestData => (TestData, Either[Throwable, A]))

object TestIO {
  implicit val monadTestIO = new Monad[TestIO]
  with MonadError[TestIO, Throwable] {
    override def pure[A](x: A): TestIO[A] =
      TestIO((testData) => (testData, Right(x)))

    override def flatMap[A, B](fa: TestIO[A])(f: A => TestIO[B]): TestIO[B] =
      TestIO(
        testData =>
          fa.run(testData) match {
            case (t, Right(a)) => f(a).run(t)
            case (t, Left(e))  => (t, Left(e))
        }
      )

    override def tailRecM[A, B](a: A)(f: A => TestIO[Either[A, B]]): TestIO[B] =
      flatMap(f(a)) {
        case Left(ar)  => tailRecM(ar)(f)
        case Right(br) => pure(br)
      }

    override def raiseError[A](e: Throwable): TestIO[A] =
      TestIO(testData => (testData, Left(e)))

    override def handleErrorWith[A](
      fa: TestIO[A]
    )(f: Throwable => TestIO[A]): TestIO[A] =
      TestIO(
        testData =>
          fa.run(testData) match {
            case (t, res @ Right(_)) => (t, res)
            case (t, Left(error))    => f(error).run(t)
        }
      )
  }

  def userDb(user: User, password: String = "1234")(
    implicit monadError: MonadError[TestIO, Throwable]
  ): UserDb[TestIO] =
    userDb(
      getUserReply = monadError.pure(user),
      createUserReply = monadError.pure(user),
      getUserForAuthReply =
        monadError.pure(UserWithPassword(user.id, password, user.description))
    )

  def userDb(getUserReply: TestIO[User],
             createUserReply: TestIO[User],
             getUserForAuthReply: TestIO[UserWithPassword]): UserDb[TestIO] =
    new UserDb[TestIO] {
      override def getUser(id: Int)(
        implicit monadError: MonadError[TestIO, Throwable]
      ): TestIO[User] = getUserReply

      override def createUser(user: UserWithPassword)(
        implicit monadError: MonadError[TestIO, Throwable]
      ): TestIO[User] = createUserReply

      override def getUserForAuth(id: Int)(
        implicit monadError: MonadError[TestIO, Throwable]
      ): TestIO[UserWithPassword] = getUserForAuthReply
    }
}
