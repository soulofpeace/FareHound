package actors

import models._

import akka.actor._

import dispatch._

import akka.util.duration._

import play.api.libs.json._

trait ExchangeRateComponent{

  this:ComponentSystem => 

  val exchangeRateComponent:ExchangeRateComponentImpl = new ExchangeRateComponentImpl

  class ExchangeRateComponentImpl{

    private class UpdateActor extends Actor{
      def receive ={
        case UpdateExchangeRate=>{
          println("updating currency")
          val http = new Http
          val updateRequest =  url("http://openexchangerates.org/api/latest.json?app_id="+System.getenv("OPEN_EXCHANGE_KEY"))
            val updateResponseJson = Json.parse(http(updateRequest OK as.String)())
            val newExchangeRates = (updateResponseJson \ "rates").as[Map[String, Float]]
            exchangeRateMap = newExchangeRates 
        }
      }
    }

    private case object UpdateExchangeRate
    private case class  NewExchangeRate(newExchangeRates:Map[String, Float])

    var exchangeRateMap = Map[String, Float]()
    val updateActor = system.actorOf(Props(new UpdateActor))

    system.scheduler.schedule(0 seconds, 1 day, updateActor, UpdateExchangeRate)

    println("done")

    def convert(amount:Float, originalCurrency:String, targetCurrency:String):Option[Float]={
      if(originalCurrency.toUpperCase == targetCurrency.toUpperCase){
        Some(amount)
      }
      else{
        for{
          originalCurrencyRate <- exchangeRateMap.get(originalCurrency.toUpperCase)
          targetCurrencyRate <- exchangeRateMap.get(targetCurrency.toUpperCase)
        }yield(amount/originalCurrencyRate * targetCurrencyRate)
      }
    }
  }
}
