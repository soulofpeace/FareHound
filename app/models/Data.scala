package models

import java.util.Date
import java.text.SimpleDateFormat

sealed trait Data

case object Uninitialized extends Data

object SearchRequest{
  def fromKey(key:String)={
    val dateFormat = new SimpleDateFormat("yyMMddHHmmssZ")
    val tokens = key.split(":")
    SearchRequest(
      tokens(0),
      tokens(1),
      dateFormat.parse(tokens(2)),
      dateFormat.parse(tokens(3)),
      tokens(4).toInt,
      tokens(5).toInt,
      tokens(6),
      tokens(7))

  }
}

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
      val dateFormat = new SimpleDateFormat("yyMMddHHmmssZ")
      origin+":"+destination+":"+dateFormat.format(departureDate)+":"+dateFormat.format(returnDate)+":"+numAdults+":"+numChildren+":"+tripType+":"+cabinClass
    }
}

case class PullData(
  searchRequest:SearchRequest,
  instanceId:Option[String],
  random:String=System.currentTimeMillis.toString,
  numPulls:Int = 0,
  minPrice:Option[CheapestPrice]=None,
  pending:List[SearchRequest]=List[SearchRequest]()
) extends Data


case class CheapestPrice(price:Float, deeplinkUrl:String) extends Data

case class Monitor(userId:String, searchRequest:SearchRequest, price:Float){
  def getKey={
    userId+":"+searchRequest.getKey
  }
}

case class User(id:String, phoneNumber:String)
