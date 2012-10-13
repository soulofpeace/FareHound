package actors

import models._

import akka.actor._
import akka.pattern.ask
import akka.dispatch.Await
import akka.actor.{Actor, ActorRef, FSM, Props}
import akka.routing.SmallestMailboxRouter

import akka.util.duration._

import akka.event.Logging

import java.util.Date
import java.text.SimpleDateFormat

import akka.actor.Actor._

import play.api.libs.json._

import dispatch._


trait SearchComponent{
  this:CheckerComponent with ExchangeRateComponent with ComponentSystem =>

  val searchActorRef = system.actorOf(Props(new SearchActor).withRouter(
      SmallestMailboxRouter(nrOfInstances = 5)))

  class SearchActor extends Actor with FSM[State, Data]{
    private val apiKey = System.getenv("WEGO_API_KEY")

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
      case Event(StartSearch(searchRequest), pullData:PullData)=>{
        goto(Active) using pullData.copy(pending=pullData.pending:+searchRequest)
      }
      case Event(StateTimeout, pullData:PullData)=>{
        if(pullData.numPulls < 10){
          val cheapestprice = doPull(pullData)
          if(cheapestprice.isDefined){
            stay using pullData.copy(
              numPulls=pullData.numPulls +1,
              minPrice = cheapestprice.get
            )
          }
          else{
            stay using pullData.copy(
              numPulls=pullData.numPulls +1
            )
          }
        }
        else{
          if(pullData.minPrice.isDefined)
            checkerActorRef!Check(pullData.searchRequest, pullData.minPrice.get)
          val nextPending = getNextPending(pullData.pending)
          if(nextPending.isDefined){
            stay using nextPending.get
          }
          else{
            goto(Idle) using Uninitialized
          }
        }
      }
    }

    private def getNextPending(pending:List[SearchRequest]):Option[PullData]={
      pending match{
        case Nil =>{
          None
        }
        case head::rest =>{
          val newPullData = doStartSearch(head)
          if(newPullData.instanceId.isDefined){
            Some(newPullData.copy(pending=rest))
          }
          else{
            getNextPending(rest)
          }
        }
      }
    }

    private def constructDeeplinkUrl(instanceId:String, bookingCode:String, origin:String, destination:String, providerId:String)={
      "http://www.wego.com/api/flights/redirect.html?apiKey="+
      apiKey +
      "&bookingCode="+
      bookingCode+
      "&providerId="+
      providerId+
      "&dlfrom="+
      origin+
      "&dlto="+
      destination+
      "&instanceId="+
      instanceId
    }

    private def getCheapestPrice(response:String, pullData:PullData)={
      //println(response)
      val responseJson = Json.parse(response)
      val itinerariesJson = (responseJson \ "response" \ "itineraries").asInstanceOf[JsArray]
      val cheapestPrice = itinerariesJson.value.foldLeft(pullData.minPrice)((cheapestPrice, itineraryJson) => {
        val result = for{
          pricePerPax <- (itineraryJson \ "price" \ "totalPricePerPassenger").asOpt[String]
          currencyCode <- (itineraryJson \ "price" \ "currencyCode").asOpt[String]
          bookingCode <- (itineraryJson \ "bookingCode").asOpt[String]
          providerId <- (itineraryJson \ "providerId").asOpt[String]
          priceInUsd <- exchangeRateComponent.convert(pricePerPax.toFloat, currencyCode, "USD")
        }yield{
          val deeplinkUrl = constructDeeplinkUrl(pullData.instanceId.get, bookingCode, pullData.searchRequest.origin,
            pullData.searchRequest.destination, providerId)
          val currentCheapest = CheapestPrice(priceInUsd, deeplinkUrl)
          val pastCheapest = cheapestPrice.getOrElse(currentCheapest)
          if (currentCheapest.price  < pastCheapest.price){
            currentCheapest
          }
          else{
            pastCheapest
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
                            }yield getCheapestPrice(json, pullData)
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
}

