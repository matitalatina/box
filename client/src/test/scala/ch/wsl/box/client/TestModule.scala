package ch.wsl.box.client

import ch.wsl.box.client.mocks.{HttpClientMock, RestMock}
import ch.wsl.box.client.services.{ClientSession, HttpClient, Navigator, REST}
import wvlet.airframe._

case class TestModule(rest:REST)  {
  val test = newDesign
    .bind[HttpClient].to[HttpClientMock]
    .bind[REST].toInstance(rest)
    .bind[ClientSession].toSingleton
    .bind[Navigator].toSingleton
}
