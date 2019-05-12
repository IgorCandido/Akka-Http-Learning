package training

import akka.actor.ActorSystem
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.StatusCodes._

import scala.io.StdIn

object ExceptionHandling {
  implicit val system = ActorSystem("Exceptions")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  implicit val exceptionHandler = ExceptionHandler {
    case error: Exception =>
    extractUri{uri =>
      println(s"Error calling ${uri} with exception ${error}")
      complete(HttpResponse(InternalServerError, entity= s"Failed calling url: ${uri}"))
    }
  }

  val route =
    path("hi") {
      complete("Hi there")
    } ~
  path("boom"){
    throw new Exception("Boom!")
  }

  def main(args: Array[String]): Unit = {
    val bindingFuture = Http().bindAndHandle(route, interface = "localhost", port= 80)

    println("Running on localhost:80")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete{_ => system.terminate()}
  }

}
