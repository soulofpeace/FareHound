package connection

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
      def attempt: A = try {
          println(client)
          client.auth(secret)
          fn(client)
        }
        catch {
          case e:com.redis.RedisConnectionException =>
            println("redis fail!")
            e.printStackTrace()
            println("attempting reconnection")
            if(client.reconnect) 
              attempt
            else {
              println("could not reconnect")
              throw e
            }
          case e =>
            println("unexpected fail!")
            e.printStackTrace()
            throw
            e
        }
        attempt
      }}
    }
  }
