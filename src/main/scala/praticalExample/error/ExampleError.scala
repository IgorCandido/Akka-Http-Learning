package praticalExample.error

sealed trait ExampleError extends Throwable
final case object UserAlreadyExisted extends ExampleError
final case class UserDoesntExist(userId: Int) extends ExampleError
final case class UnknownError(throwable: Throwable) extends ExampleError
