package actors

import akka.actor._
import serializers._
import daos._
import models._

trait SchedulerComponent {
  this:SearchComponent with Store  with ComponentSystem=>

  val scheduler = system.actorOf(Props(new SchedulerActor))

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
