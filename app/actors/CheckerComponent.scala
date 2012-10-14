package actors

import models._

import daos.Store
import serializers.SerializerComponent

import akka.actor._
import akka.event.Logging
import akka.routing.SmallestMailboxRouter

trait CheckerComponent {
  this:Store with BestPricerComponent with ComponentSystem=> 

  val checkerActorRef = system.actorOf(Props(new CheckerActor).withRouter(
      SmallestMailboxRouter(nrOfInstances = 2)))

  class CheckerActor extends Actor{

    val log = Logging(context.system, this)
    def receive={
      case Check(searchRequest:SearchRequest, cheapestPrice:CheapestPrice)=>{
        log.info("Receive :"+ searchRequest+ " with: "+cheapestPrice)
        val monitors = store.getMonitorBySearchRequest(searchRequest)
        monitors.foreach(monitor =>{
          if(monitor.price > cheapestPrice.price){
            store.storeMonitor(monitor.copy(price=cheapestPrice.price))
            store.getUser(monitor.userId).map ( user => {
              bestPricerComponent!CheckBestPrice(Notify(user, cheapestPrice, searchRequest, cheapestPrice.price))
            })
          }
        })
      }
    }
  }
}
