package ch.wsl.box.services

import ch.wsl.box.services.files.ImageCache
import ch.wsl.box.services.mail.MailService
import wvlet.airframe._

trait Services {
  val imageCacher = bind[ImageCache]
  val mail = bind[MailService]
}
