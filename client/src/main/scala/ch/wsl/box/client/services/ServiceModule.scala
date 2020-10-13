package ch.wsl.box.client.services

import ch.wsl.box.client.Context

class ServiceModule(context:Context) {
  import com.softwaremill.macwire._

  lazy val httpClient = wire[HttpClient]
  lazy val rest = wire[REST]
  lazy val session = wire[Session]
  lazy val navigator = wire[Navigator]
}
