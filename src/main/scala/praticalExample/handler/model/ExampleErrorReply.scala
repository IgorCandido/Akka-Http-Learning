package praticalExample.handler.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

final case class ErrorReply(message: String, errorCode: Int)

object ErrorTypes {
  val UserAlreadyExists = 1
  val UserDoesntExist = 2
  val UnknownError = 99
}

object ExampleErrorJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val errorReplyFormat = jsonFormat2(ErrorReply)
}