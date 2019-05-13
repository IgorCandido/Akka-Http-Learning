package training

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.StdIn

object PostWithBody {

  implicit val system = ActorSystem("bodies")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  case class RequestInfo(name: String, description: String)
  case class InfoReply(name: String,
                       description: String,
                       age: Int,
                       height: Double)

  object jsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val requestInfoFormat = jsonFormat2(RequestInfo)
    implicit val infoReplyFormat = jsonFormat4(InfoReply)
  }

  import jsonSupport._
  val route = post {
    path("infoEnquire") {
      entity(as[RequestInfo]) { requestInfo =>
        complete(
          InfoReply(
            name = requestInfo.name,
            description = requestInfo.description,
            age = 42,
            height = 1.5
          )
        )
      }

    }
  }

  def main(args: Array[String]): Unit = {

    val binding = Http().bindAndHandle(route, interface = "localhost", port = 80)

    println("Running on localhost:80")
    StdIn.readLine()
    binding.flatMap(_.unbind()).onComplete{_ => system.terminate()}
  }

}
