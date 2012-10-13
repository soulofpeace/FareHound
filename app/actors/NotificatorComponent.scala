package actors

import akka.actor._
import akka.routing.SmallestMailboxRouter

import models._

trait NotificatorComponent{
  this:ComponentSystem =>
  val notificatorActorRef = system.actorOf(Props(new NotificatorActor).withRouter(
      SmallestMailboxRouter(nrOfInstances = 5)))

  class NotificatorActor extends Actor{
    def receive={
      case Notify(user:User, cheapestPrice:CheapestPrice, searchRequest:SearchRequest, bestPrice:Float)=>{
        println("sending to phonenumber "+ user.phoneNumber + "for "+searchRequest.origin +" to "+ searchRequest.destination + " with "+
          "price "+cheapestPrice.price + " at "+cheapestPrice.deeplinkUrl)
        controllers.Sms.sendPriceAlert(user.phoneNumber, cheapestPrice.price, bestPrice, cheapestPrice.deeplinkUrl)
      }
    }
  }
}
