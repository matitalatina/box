package ch.wsl.box.client.services

import wvlet.airframe._

trait ServiceModule {
  val httpClient = bind[HttpClient]
  val rest = bind[REST]
  val clientSession = bind[ClientSession]
  val navigator  = bind[Navigator]
}
