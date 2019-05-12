package util

import cats.{Monad, MonadError}
import cats.implicits._
import praticalExample.error.ExampleError
import praticalExample.service.UserDb
import praticalExample.service.model.User

import scala.annotation.tailrec

case class TestIO[A](value: Option[A], error: Option[Throwable] = None)

object TestIO {
  implicit val monadTestIO = new Monad[TestIO] with MonadError[TestIO, Throwable] {
    override def pure[A](x: A): TestIO[A] = TestIO(x.some)

    override def flatMap[A, B](fa: TestIO[A])(f: A => TestIO[B]): TestIO[B] =
      f(fa.value.get)

    @tailrec
    override def tailRecM[A, B](
      a: A
    )(f: A => TestIO[Either[A, B]]): TestIO[B] = {
      f(a) match {
        case TestIO(Some(Right(valueB)), _) => TestIO(valueB.some)
        case TestIO(None,_) => TestIO(None)
        case TestIO(Some(Left(valueA)), _)  => tailRecM(valueA)(f)
      }
    }

    override def raiseError[A](e: Throwable): TestIO[A] = TestIO(None, e.some)

    override def handleErrorWith[A](fa: TestIO[A])(f: Throwable => TestIO[A]): TestIO[A] = f(fa.error.get)
  }

  def userDb(userValue: User): UserDb[TestIO] = new UserDb[TestIO] {
    override def getUser(id: Int)(
      implicit monadError: MonadError[TestIO, Throwable]
    ): TestIO[User] = monadError.pure(userValue)

    override def createUser(user: User)(
      implicit monadError: MonadError[TestIO, Throwable]
    ): TestIO[User] = monadError.pure(userValue)
  }
}
