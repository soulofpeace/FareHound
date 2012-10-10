package driver

import com.redis._

object RedisConnectionFactory {
  private val clients = new RedisClientPool("localhost", 6379)

  def withConnection[A](fn:RedisClient=>A)={
    clients.withClient{client =>{
      fn(client)
    }}
  }
}
