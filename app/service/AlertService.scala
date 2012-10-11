package service

import akka.actor._
import akka.routing.SmallestMailboxRouter

import daos.impl.RedisStore
import serializers.impl.KryoSerializer
import actors._


object AlertService 
extends SearchComponent
with CheckerComponent
with NotificatorComponent
with RedisStore
with KryoSerializer{

  val system = ActorSystem("FareHound")
  val store = new RedisStoreImpl
  val serializer = new KryoSerializerImpl
  val searchActorRef = system.actorOf(Props[SearchActor].withRouter(
      SmallestMailboxRouter(nrOfInstances = 5)))
  val checkerActorRef = system.actorOf(Props[CheckerActor].withRouter(
      SmallestMailboxRouter(nrOfInstances = 5)))
  val notificatorActorRef = system.actorOf(Props[CheckerActor].withRouter(
      SmallestMailboxRouter(nrOfInstances = 5)))
}
