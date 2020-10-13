package ch.wsl.box.client
import ch.wsl.box.client.services.{ClientSession, HttpClient, Navigator, REST}
import ch.wsl.box.client.services.impl.{HttpClientImpl, RestImpl}
import wvlet.airframe._

object Module {
  val prod = newDesign
    .bind[HttpClient].to[HttpClientImpl]
    .bind[REST].to[RestImpl]
    .bind[ClientSession].toEagerSingleton
    .bind[Navigator].toEagerSingleton
}
