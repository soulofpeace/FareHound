package daos

import models._
import serializers._

trait Store extends SerializerComponent{
  val store:StoreImpl
  trait StoreImpl {
    def getAllSearchKeys:List[String]
    def getMonitorBySearchRequest(searchRequest:SearchRequest):List[Monitor]
    def storeMonitor(user:User, monitor:Monitor):Unit
    def storeUser(user:User):Unit
    def getNextUserId:String
  }
}
