package ch.wsl.box.rest

import ch.wsl.box.services.Services
import ch.wsl.box.services.file.ImageCacheStorage
import ch.wsl.box.services.files.{ InMemoryImageCacheStorage, PgImageCacheStorage}
import wvlet.airframe._

trait Module{
  def injector:Design
}

object DefaultModule extends Module {

  val injector = newDesign
    .bind[ImageCacheStorage].to[PgImageCacheStorage]
    .bind[Services].toEagerSingleton

}
