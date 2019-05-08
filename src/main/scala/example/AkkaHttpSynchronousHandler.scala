package example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.scaladsl.Sink

object AkkaHttpSynchronousHandler {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val serverSource = Http().bind(interface = "localhost", port = 8080)

    val requestHandler: HttpRequest => HttpResponse = {
      case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
        HttpResponse(
          entity = HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            "<html><body>Hello world!</body></html>"
          )
        )
      case HttpRequest(GET, Uri.Path("/ping"), _, _, _) =>
        HttpResponse(entity = "PONG!")

      case HttpRequest(GET, Uri.Path("/crash"), _, _, _) =>
        sys.error("BOOM!")

      case r: HttpRequest =>
        r.discardEntityBytes()
        HttpResponse(404, entity = "Unknown resource!")
    }

    serverSource.to(Sink.foreach{ connection =>
      print("Accepted new connection from " + connection.remoteAddress)

      connection handleWithSyncHandler requestHandler
    }).run()
  }

}
