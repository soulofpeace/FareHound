package actors

import models._

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging

class CheckerActor extends Actor{
  def receive={
    case Check(searchRequest:SearchRequest, cheapestPrice:CheapestPrice)=>{
    }
  }
}
