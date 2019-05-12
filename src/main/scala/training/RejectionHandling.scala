package training

import java.util.concurrent.ExecutorService

import akka.actor.ActorSystem
import akka.dispatch.affinity.RejectionHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.http.scaladsl.server.{RejectionHandler, Route}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.HttpMethods._

import scala.io.StdIn

object RejectionHandling {
  implicit val system = ActorSystem("handling")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  implicit val rejectionHandler = RejectionHandler
    .newBuilder()
    .handleNotFound {

      complete((StatusCodes.NotFound, "You did not enter the right path on your thing portal"))
    }
    .result()

  val route: Route =
      path("hi") {
        complete("True hi")
      } ~ path("goodbye") {
        complete("Goodbye hello")
      }

  def main(args: Array[String]): Unit = {
    val binding =
      Http().bindAndHandle(route, interface = "localhost", port = 80)

    println("Running on localhost:80")
    StdIn.readLine()
    binding.flatMap { _.unbind() }.onComplete { _ =>
      system.terminate()
    }

  }

}
