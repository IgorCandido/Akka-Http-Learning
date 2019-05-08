package example

object LongerExample {

  def main(args: Array[String]) ={
    import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    import spray.json.DefaultJsonProtocol._

    type Money = Double
    type TransactionResult = String
    case class User(name: String)
    case class Order(email: String, amount: Money)
    case class Update(order: Order)
    case class OrderItem(i: Int, os: Option[String], s: String)

    object Formats{
      implicit val orderformatter = jsonFormat2(Order)
    }

  }

}
