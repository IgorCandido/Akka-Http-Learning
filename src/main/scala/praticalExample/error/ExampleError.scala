package praticalExample.error

sealed trait ExampleError extends Throwable
final case object UserAlreadyExisted extends ExampleError

