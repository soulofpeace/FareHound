package actors

import models._

import daos.Store
import serializers.SerializerComponent

import akka.actor._

trait CheckerComponent extends Store with SerializerComponent with NotificatorComponent{
  val checkerActorRef:ActorRef

  class CheckerActor extends Actor{

    def receive={
      case Check(searchRequest:SearchRequest, cheapestPrice:CheapestPrice)=>{
        val monitors = store.getMonitorBySearchRequest(searchRequest)
        monitors.foreach(monitor =>{
          if(monitor.price > cheapestPrice.price){
            store.storeMonitor(monitor.copy(price=cheapestPrice.price))
            store.getUser(monitor.userId).map ( user => {
              notificatorActorRef!Notify(user, cheapestPrice, searchRequest)
            })
          }
        })
      }
    }
  }
}
