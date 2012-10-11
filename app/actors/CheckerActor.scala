package actors

import models._

import daos.impl.RedisStore
import serializers.impl.KryoSerializer

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

class CheckerActor extends Actor with RedisStore with KryoSerializer{
  val store =  new RedisStoreImpl
  val serializer = new KryoSerializerImpl

  def receive={
    case Check(searchRequest:SearchRequest, cheapestPrice:CheapestPrice)=>{
      val monitors = store.getMonitorBySearchRequest(searchRequest)
      monitors.foreach(monitor =>{
        if(monitor.price > cheapestPrice.price){
          //code to send alert
          store.storeMonitor(monitor.copy(price=cheapestPrice.price))
        }
      })
    }
  }
}
