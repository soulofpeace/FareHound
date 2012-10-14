package actors

import akka.actor._
import akka.routing.SmallestMailboxRouter

import models._

import dispatch._

import play.api.libs.json._

trait BestPricerComponent{
  this:NotificatorComponent with ExchangeRateComponent with ComponentSystem=> 

  val bestPricerComponent = system.actorOf(Props(new BestPricerActor).withRouter(
      SmallestMailboxRouter(nrOfInstances = 2)))

  class BestPricerActor extends Actor{

    private lazy val http = new Http 

    def receive={
      case CheckBestPrice(notify)=>{
        val searchRequest = notify.searchrequest
        val bestPriceUrl = url("http://www.wego.com.sg/flights/api/rates/from_city_to_city")
        val bestPricerRequest =  bestPriceUrl <<? Map(
          "from" -> searchRequest.origin,
          "to"   -> searchRequest.destination,
          "trip_type" -> searchRequest.tripType.toLowerCase,
          "key"  -> System.getenv("WEGO_API_KEY")
        )
        val bestPricerResponse = http(bestPricerRequest OK as.String).option
        val bestPrice = for( res <- bestPricerResponse)
                          yield for{
                            json <- res
                          }yield {
                            val bestPriceJson =  Json.parse(json)
                            val bestPrices = (bestPriceJson \ "list").asInstanceOf[JsArray]
                            for {
                              firstBestPrice <- bestPrices.value.headOption
                              amount <- (firstBestPrice \ "amount").asOpt[String]
                              currency <- (firstBestPrice \ "currencyCode").asOpt[String]
                            }
                            yield exchangeRateComponent.convert(amount.toFloat, currency, "USD")
                          }
        val bestPriceResult = bestPrice().flatten.flatten

        if(!bestPriceResult.isEmpty){
          notificatorActorRef!notify.copy(bestprice = bestPriceResult.head)
        }
        else{
          notificatorActorRef!notify
        }
      }
    }
  }
}
