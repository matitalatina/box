package ch.wsl.box.services

import ch.wsl.box.services.files.ImageCache
import wvlet.airframe._

trait Services {
  val imageCacher = bind[ImageCache]
}
