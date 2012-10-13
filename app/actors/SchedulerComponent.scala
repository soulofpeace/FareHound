package actors

import akka.actor._
import serializers._
import daos._
import models._

import akka.event.Logging

trait SchedulerComponent {
  this:SearchComponent with Store  with ComponentSystem=>

  val scheduler = system.actorOf(Props(new SchedulerActor))

  class SchedulerActor extends Actor{
    val log = Logging(context.system, this)
    def receive={
      case CheckMonitor=>{
        log.info("Checking all monitor")
        store.getAllSearchKeys.foreach(key =>{
          val request = SearchRequest.fromKey(key)
          searchActorRef!StartSearch(request)
        })
      }
    }
  }
}
