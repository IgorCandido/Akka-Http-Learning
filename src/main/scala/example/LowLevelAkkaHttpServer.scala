package example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.stream.scaladsl._

import scala.concurrent.Future

object LowLevelAkkaHttpServer {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val serverSource
      : Source[Http.IncomingConnection, Future[Http.ServerBinding]] =
      Http().bind(interface = "localhost", port = 8080)

    val bindingFuture: Future[Http.ServerBinding] = serverSource.to(Sink.foreach{ connection =>
      println("Accepted new connection from " + connection.remoteAddress)
    }).run()

  }

}
