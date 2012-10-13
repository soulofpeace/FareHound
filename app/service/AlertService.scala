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

  //val serializer = new JavaSerializerImpl
  //val store = new RedisStoreImpl
  //val exchangeRateComponent = new ExchangeRateComponentImpl 
  //val notificatorActorRef = system.actorOf(Props(new NotificatorActor).withRouter(
      //SmallestMailboxRouter(nrOfInstances = 5)))
  //val bestPricerComponent = system.actorOf(Props(new BestPricerActor).withRouter(
      //SmallestMailboxRouter(nrOfInstances = 5)))
  //val checkerActorRef = system.actorOf(Props(new CheckerActor).withRouter(
      //SmallestMailboxRouter(nrOfInstances = 5)))
  //val searchActorRef = system.actorOf(Props(new SearchActor).withRouter(
      //SmallestMailboxRouter(nrOfInstances = 5)))
  //val scheduler = system.actorOf(Props(new SchedulerActor))

  //system.scheduler.schedule(0 seconds, 1 hour, scheduler, CheckMonitor)

  def register(
    phoneNumber:String,
    origin:String, 
    destination:String, 
    departureDate:Date,
    price:Float){

      val user = User(phoneNumber, phoneNumber)
      val searchRequest = SearchRequest(origin, destination, departureDate)
      val monitor = new Monitor(user.id, searchRequest, price)
      store.storeUser(user)
      store.storeMonitor(monitor)
      searchActorRef!StartSearch(searchRequest)
  }
}
