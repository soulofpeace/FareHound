package models

import java.util.Date

sealed trait Data

case object Uninitialized extends Data

case class SearchRequest(
  origin:String,
  destination:String,
  departureDate:Date,
  returnDate:Date=new Date(),
  numAdults:Int = 1,
  numChildren:Int = 0,
  tripType:String="oneWay",
  cabinClass:String="Economy") extends Data{

    def getKey={
      origin+":"+destination+":"+departureDate.toString+":"+returnDate.toString+":"+numAdults+":"+numChildren+":"+tripType+":"+cabinClass
    }
}

case class PullData(
  searchRequest:SearchRequest,
  instanceId:Option[String],
  random:String=System.currentTimeMillis.toString,
  numPulls:Int = 0,
  minPrice:Option[CheapestPrice]=None
) extends Data


case class CheapestPrice(price:Float, deeplinkUrl:String) extends Data

case class Monitor(userId:String, searchRequest:SearchRequest, price:Float){
  def getKey={
    userId+":"+searchRequest.getKey
  }
}

case class User(id:String, phoneNumber:String)
