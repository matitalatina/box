package ch.wsl.box.cache.redis

import ch.wsl.box.rest.{Boot, DefaultModule}
import ch.wsl.box.services.files.ImageCacheStorage

object ServeWithRedis extends App {

  val module = DefaultModule.injector.bind[ImageCacheStorage].to[RedisImageCacheStorage]

  Boot.run("Redis test","redis test",module)
}
