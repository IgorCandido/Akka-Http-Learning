package praticalExample.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import praticalExample.handler.UserHandler

import scala.io.StdIn

object AkkaServer {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("pratical")
    implicit val materializer = ActorMaterializer()
    implicit val dispatcher = system.dispatcher

    val binding = Http().bindAndHandle(UserHandler.route, interface = "localhost", port = 80)

    println("Running on localhost:80")
    StdIn.readLine()
    binding.flatMap(_.unbind()).onComplete{ _ => system.terminate()}
  }

}
