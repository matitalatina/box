package ch.wsl.box.cache.redis

import akka.actor.ActorSystem
import ch.wsl.box.services.file.{FileCacheKey, FileId, ImageCacheStorage}
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import scredis._
import scredis.protocol.AuthConfig

import scala.concurrent.{ExecutionContext, Future}

class RedisImageCacheStorage extends ImageCacheStorage {

  implicit val system = ActorSystem("redis-actor-system")

  val redisConf = ConfigFactory.load().as[Config]("redis")

  val auth = for{
    username <- redisConf.as[Option[String]]("username")
    password <- redisConf.as[Option[String]]("password")
  } yield AuthConfig(Some(username),password)


  val client = Client(
    host = redisConf.getString("host"),
    port = redisConf.getInt("port"),
    authOpt = auth
  )

  override def clearField(id: FileId)(implicit ex: ExecutionContext): Future[Boolean] = {
    for{
      keys <- client.keys(id.asString("")+"*")
      _ <- client.del({keys.toSeq}:_*)
    } yield true

  }

  override def save(fileId: FileCacheKey, data: Array[Byte])(implicit ex: ExecutionContext): Future[Boolean] = {
    client.set(fileId.asString,data)
  }

  override def delete(fileId: FileCacheKey)(implicit ex: ExecutionContext): Future[Boolean] = {
    client.del(fileId.asString()).map(_ > 0)
  }

  override def get(fileId: FileCacheKey)(implicit ex: ExecutionContext): Future[Option[Array[Byte]]] = {
    import scredis.serialization.Implicits.bytesReader
    client.get[Array[Byte]](fileId.asString())
  }
}
