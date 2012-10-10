package daos.impl

import daos.Store

import com.redis._

import driver.RedisConnectionFactory

import models._

trait RedisStore extends Store{
  class RedisStoreImpl extends StoreImpl{
    def getAllSearchKeys={
      RedisConnectionFactory.withConnection((client:RedisClient)=>{
        client.smembers("searchKeys").toList
      })
    }
    def getMonitorBySearchRequest(searchRequest:SearchRequest)={
      RedisConnectionFactory.withConnection((client:RedisClient) =>{
        val searchKey = searchRequest.getKey
        for{
          registeredMonitors <- client.smembers(searchKey)
        }yield registeredMonitors.map
        val registeredMonitors = client.smembers(searchKey).toList.map(key => key map{
          k =>{
            serializer.deserialize[Monitor](client.get(k.getBytes("UTF-8")))
          }
        }).toList
      })
    }

    def storeMonitor(user:User, monitor:Monitor){
      RedisConnectionFactory.withConnection((client:RedisClient)=>{
        val searchKey = monitor.searchRequest.getKey
        val monitorKey = monitor.getKey
        client.pipeline { p =>{
          p.sadd("searchKeys", searchKey)
          p.sadd(searchKey, monitorKey)
          p.set(monitorKey.getBytes("UTF-8"), serializer.serialize(monitor))
        }}
      })
    }

    def storeUser(user:User){
      RedisConnectionFactory.withConnection((client:RedisClient)=>{
        client.hset(user.id, "phone_number", user.phoneNumber)
      })
    }

    def getNextUserId={
      RedisConnectionFactory.withConnection((client:RedisClient)=>{
        client.pipeline { p =>{
          client.incr("next_user_id")
          client.get("next_user_id")
        }}.get(1).get
      })
    }
  }
}
