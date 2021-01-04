package ch.wsl.box.rest

import ch.wsl.box.services.Services
import ch.wsl.box.services.files.{ImageCacheStorage, InMemoryImageCacheStorage}
import wvlet.airframe._

trait Module{
  def injector:Design
}

object DefaultModule extends Module {

  val injector = newDesign
    .bind[ImageCacheStorage].to[InMemoryImageCacheStorage]
    .bind[Services].toEagerSingleton

}
