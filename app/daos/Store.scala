package daos

import models._
import serializers._

trait Store extends SerializerComponent{
  val store:StoreImpl
  trait StoreImpl {
    def getAllSearchKey:List[String]
    def getMonitorBySearchRequest(searchRequest:SearchRequest):List[Monitor]
    def storeMonitor(user:User, monitor:Monitor):Unit
    def storeUser(user:User):Unit
    def getNextUserId:Int
  }
}
