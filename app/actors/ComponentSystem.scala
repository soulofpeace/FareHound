package actors

import akka.actor._

trait ComponentSystem{
  val system:ActorSystem = ActorSystem("FareHound")
}
