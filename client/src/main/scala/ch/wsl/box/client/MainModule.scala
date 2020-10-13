package ch.wsl.box.client

import ch.wsl.box.client.services.ServiceModule

trait MainModule {
  import com.softwaremill.macwire._

  lazy val context = wire[Context]
  lazy val services = wire[ServiceModule]

}
