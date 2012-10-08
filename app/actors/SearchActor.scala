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
class SearchActor extends FSM[SearchActorState, Data]{

  private val log = Logging(context.system, this)
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
      if(pullData.numPulls < 10){
        doPull(pullData)
        stay using pullData
      }
      else{
        goto(Idle) using Uninitialized
      }
    }
  }

  private def doPull(pullData:PullData)={
    val pullUrl = url("http://www.wego.com/api/flights/pull.html")
    val pullRequest = pullUrl <<? Map(
      "instanceId" -> pullData.instanceId.get,
      "format" -> "json",
      "rand" -> System.currentTimeMillis.toString,
      "apiKey" -> "testAcc"
    )
    val pullResponse = Http(pullRequest OK as.String)
    val json = for(res<-pullResponse) yield Json.parse(res)
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
      "departureDate" -> format.format(searchRequest.departureDate),
      "returnDate"    -> format.format(searchRequest.returnDate),
      "apiKey"        -> "testAcc"
      )


    val startSearchResponse = Http(startSearchRequest OK as.String)
    val str  = for(response <- startSearchResponse) yield response
    val instanceIdRegex = """.*?"instanceId".*?:.*?"(.*?)".*?""".r
    val instanceIdMatch = instanceIdRegex.findFirstMatchIn(str.get)
    val instanceId = instanceIdMatch.map( _.group(1))

    PullData(searchRequest, instanceId)

  }
}

