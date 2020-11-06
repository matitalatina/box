package ch.wsl.box.client

import ch.wsl.box.client.mocks.{HttpClientMock, RestMock}
import ch.wsl.box.client.services.{ClientSession, HttpClient, Navigator, REST}
import wvlet.airframe._

object TestModule {
  val test = newDesign
    .bind[HttpClient].to[HttpClientMock]
    .bind[REST].to[RestMock]
    .bind[ClientSession].toSingleton
    .bind[Navigator].toSingleton
}
