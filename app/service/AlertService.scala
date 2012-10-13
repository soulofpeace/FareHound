package service

import akka.actor._
import akka.routing.SmallestMailboxRouter

import daos.impl.RedisStore
import serializers.impl.JavaSerializer

import akka.util.duration._

import actors._
import models._

import java.util.Date


object AlertService 
extends ComponentSystem
with ExchangeRateComponent
with JavaSerializer
with RedisStore
with NotificatorComponent
with BestPricerComponent
with CheckerComponent
with SearchComponent
with SchedulerComponent{


  system.scheduler.schedule(0 seconds, 30  minutes, scheduler, CheckMonitor)

  def register(
    phoneNumber:String,
    origin:String, 
    destination:String, 
    departureDate:Date,
    price:Float){

      val user = User(phoneNumber, phoneNumber)
      val searchRequest = SearchRequest(origin, destination, departureDate, returnDate=departureDate)
      val monitor = new Monitor(user.id, searchRequest, price)
      store.storeUser(user)
      store.storeMonitor(monitor)
      searchActorRef!StartSearch(searchRequest)
  }
}
