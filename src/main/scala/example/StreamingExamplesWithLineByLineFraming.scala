package example

import akka.NotUsed
import akka.http.scaladsl.common.{
  EntityStreamingSupport,
  JsonEntityStreamingSupport
}
import akka.http.scaladsl.server.Directives.{complete, path}
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString

object StreamingExamplesWithLineByLineFraming {
  case class Tweet(uid: Int, txt: String)
  object JsonSupport
      extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
      with spray.json.DefaultJsonProtocol {

    implicit val tweetFormat = jsonFormat2(Tweet.apply)
  }

  val newline = ByteString("\n")

  import JsonSupport._
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport =
    EntityStreamingSupport
      .json()
      .withFramingRenderer(Flow[ByteString].map(bs => bs ++ newline))
      .withParallelMarshalling(parallelism = 8, unordered = false)

  val tweets = Seq(
    Tweet(1, "One"),
    Tweet(2, "Two"),
    Tweet(3, "Three"),
    Tweet(4, "Four"),
    Tweet(5, "Five")
  )

  private def getTweets() = {
    Source.fromIterator(() => Iterator(tweets: _*))
  }

  val route = path("tweets") {
    val tweets: Source[Tweet, NotUsed] = getTweets
    complete(tweets)
  }
}
