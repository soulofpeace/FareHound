package actors

import akka.actor._
import akka.event.Logging
import akka.routing.SmallestMailboxRouter

import models._

trait NotificatorComponent{
  this:ComponentSystem =>
  val notificatorActorRef = system.actorOf(Props(new NotificatorActor).withRouter(
      SmallestMailboxRouter(nrOfInstances = 2)))

  class NotificatorActor extends Actor{
    val log = Logging(context.system, this)
    def receive={
      case Notify(user:User, cheapestPrice:CheapestPrice, searchRequest:SearchRequest, bestPrice:Float)=>{
        log.info("sending to phonenumber "+ user.phoneNumber + "for "+searchRequest.origin +" to "+ searchRequest.destination + " with "+
          "price "+cheapestPrice.price + " at "+cheapestPrice.deeplinkUrl + "with bestPrice "+bestPrice)
        //println("sending to phonenumber "+ user.phoneNumber + "for "+searchRequest.origin +" to "+ searchRequest.destination + " with "+
          //"price "+cheapestPrice.price + " at "+cheapestPrice.deeplinkUrl)
        controllers.Sms.sendPriceAlert(user.phoneNumber, cheapestPrice.price, bestPrice, cheapestPrice.deeplinkUrl)
      }
    }
  }
}
