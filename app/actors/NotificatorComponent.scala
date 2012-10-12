package actors

import akka.actor._
import models._

trait NotificatorComponent {
  val notificatorActorRef:ActorRef

  class NotificatorActor extends Actor{
    def receive={
      case Notify(user:User, cheapestPrice:CheapestPrice, searchRequest:SearchRequest)=>{
        println("sending to phonenumber "+ user.phoneNumber + "for "+searchRequest.origin +" to "+ searchRequest.destination + " with "+
          "price "+cheapestPrice.price + " at "+cheapestPrice.deeplinkUrl)
      }
    }
  }
}
