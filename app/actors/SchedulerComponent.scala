package actors

import akka.actor._
import daos._
import models._

trait SchedulerComponent extends SearchComponent with Store{
  val scheduler:ActorRef

  class SchedulerActor extends Actor{
    def receive={
      case CheckMonitor=>{
        store.getAllSearchKeys.foreach(key =>{
          val request = SearchRequest.fromKey(key)
          searchActorRef!StartSearch(request)
        })
      }
    }
  }
}
