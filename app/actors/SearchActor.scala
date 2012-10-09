package actors

import models._

import akka.actor._
import akka.actor.{Actor, ActorRef, FSM, Props}

import akka.util.duration._

import akka.event.Logging

import java.util.Date
import java.text.SimpleDateFormat

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

import play.api.libs.json._

import dispatch._

object SearchActor{
  def get={
    val system = ActorSystem("FareHound")
    system.actorOf(Props[SearchActor], name = "searchActor")
  }
}

class SearchActor extends Actor with FSM[State, Data]{
  private val apiKey = "testAcc"

  private lazy val http = new Http 

  startWith(Idle, Uninitialized)

  when(Idle){
    case Event(StartSearch(searchRequest), _)=>{
      val pullData = doStartSearch(searchRequest)
      if(pullData.instanceId.isDefined){
        goto(Active) using pullData
      }
      else{
        goto(Idle) using Uninitialized
      }
    }
  }

  when(Active, stateTimeout = 5 seconds){
    case Event(StateTimeout, pullData:PullData)=>{
      if(pullData.numPulls < 1){
        doPull(pullData)
        stay using pullData.copy(numPulls=pullData.numPulls +1)
      }
      else{
        goto(Idle) using Uninitialized
      }
    }
  }

  private def constructDeeplinkUrl(instanceId:String, bookingCode:String, origin:String, destination:String, providerId:String)={
    "http://www.wego.com/api/flights/redirect.html?format=json&apiKey="+
    apiKey +
    "&bookingCode="+
    bookingCode+
    "&providerId="+
    providerId+
    "&dlfrom="+
    origin+
    "&dlto="+
    destination+
    "instanceId="+
    instanceId
  }

  private def getCheapest(response:String, pullData:PullData)={
    println(response)
    val responseJson = Json.parse(response)
    val itinerariesJson = (responseJson \ "response" \ "itineraries").asInstanceOf[JsArray]
    val cheapestPrice = itinerariesJson.value.foldLeft(None:Option[Float])((cheapestPrice, itineraryJson) => {
      val result = for{
        pricePerPax <- (itineraryJson \ "price" \ "totalPricePerPassenger").asOpt[String]
        currencyCode <- (itineraryJson \ "price" \ "currencyCode").asOpt[String]
        bookingCode <- (itineraryJson \ "bookingCode").asOpt[String]
        providerId <- (itineraryJson \ "providerId").asOpt[String]

      }yield{
        if(cheapestPrice.isDefined && pricePerPax.toFloat > cheapestPrice.get){
          cheapestPrice.get
        }
        else{
          pricePerPax.toFloat
        }
      }
      if(result.isDefined){
        result
      }
      else{
        cheapestPrice
      }
    })
    println("Cheapest: "+cheapestPrice)
    cheapestPrice
  }

  private def doPull(pullData:PullData)={
    val pullUrl = url("http://www.wego.com/api/flights/pull.html")
    val pullRequest = pullUrl <<? Map(
      "instanceId" -> pullData.instanceId.get,
      "format" -> "json",
      "rand" -> pullData.random,
      "apiKey" -> apiKey
    )
    val pullResponse = http(pullRequest OK as.String).option
    val cheapestPrice = for{res<-pullResponse}
                          yield for{
                            json <- res
                          }yield getCheapest(json, pullData)
    cheapestPrice()
  }

  private def doStartSearch(searchRequest:SearchRequest)={
    val format = new SimpleDateFormat("yyyy-MM-dd")
    val startSearchUrl = url("http://www.wego.com/api/flights/startSearch.html")
    val startSearchRequest =  startSearchUrl <<? Map(
      "fromLocation"  -> searchRequest.origin,
      "toLocation"    -> searchRequest.destination,
      "tripType"      -> searchRequest.tripType,
      "cabinClass"    -> searchRequest.cabinClass,
      "numAdults"     -> searchRequest.numAdults.toString,
      "numChildren"   -> searchRequest.numChildren.toString,
      "outboundDate"  -> format.format(searchRequest.departureDate),
      "inboundDate"   -> format.format(searchRequest.returnDate),
      "apiKey"        -> apiKey
      )


    val instanceIdRegex = """.*?"instanceId".*?:.*?"(.*?)".*?""".r
    val startSearchResponse = http(startSearchRequest OK as.String).option
    //println(for(response <- startSearchResponse) yield response)
    val instanceId  = for{
      response <- startSearchResponse
    } yield {
      for{
          json <- response
          instanceIdMatch <- instanceIdRegex.findFirstMatchIn(json)
      }yield instanceIdMatch.group(1)
    }
    PullData(searchRequest, instanceId())

  }

  initialize
}

