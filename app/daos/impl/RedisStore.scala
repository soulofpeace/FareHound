package daos.impl

import daos.Store

import com.redis._

import driver.RedisConnectionFactory

import models._

trait RedisStore extends Store{
  class RedisStoreImpl extends StoreImpl{
    def getAllSearchKeys={
      RedisConnectionFactory.withConnection((client:RedisClient)=>{
        client.smembers("searchKeys").getOrElse(Set[String]()).toList.map(_.toString)
      })
    }

    def getMonitorBySearchRequest(searchRequest:SearchRequest)={
      import com.redis.serialization.Parse.Implicits.parseByteArray
      RedisConnectionFactory.withConnection((client:RedisClient) =>{
        val searchKey = searchRequest.getKey
        client.smembers(searchKey).getOrElse(Set[String]()).toList.map(
          monitorKey =>{
            client.get[Array[Byte]](monitorKey.toString).map(bytes =>{
              serializer.deserialize[Monitor](bytes)
            })
          }
        ).flatten
      })
    }

    def storeMonitor(monitor:Monitor){
      RedisConnectionFactory.withConnection((client:RedisClient)=>{
        val searchKey = monitor.searchRequest.getKey
        val monitorKey = monitor.getKey
        client.pipeline { p =>{
          p.sadd("searchKeys", searchKey)
          p.sadd(searchKey, monitorKey)
          p.set(monitorKey, serializer.serialize(monitor))
        }}
      })
    }

    def storeUser(user:User){
      RedisConnectionFactory.withConnection((client:RedisClient)=>{
        client.hset(user.id, "phone_number", user.phoneNumber)
      })
    }

    def getUser(userId:String):Option[User]={
      RedisConnectionFactory.withConnection((client:RedisClient)=>{
        client.hget(userId, "phone_number").map( phone => User(userId, phone))
      })
    }

  }
}