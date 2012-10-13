package driver

import com.redis._
import java.net.URI

object RedisConnectionFactory {
  private val redisUri = new URI(System.getenv("REDIS_URL"))
  private val host = redisUri.getHost
  private val port = redisUri.getPort
  private val secret = redisUri.getUserInfo.split(":", 2)(1)
  private val clients = new RedisClientPool(host, port)

  def withConnection[A](fn:RedisClient=>A)={
    clients.withClient{client =>{
      client.auth(secret)
      fn(client)
    }}
  }
}
