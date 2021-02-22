package ch.wsl.box.model

import ch.wsl.box.rest.runtime.RegistryInstance

import scala.util.Try

object BoxRegistry {
  val generated:Option[RegistryInstance] = Try(Class.forName("ch.wsl.box.generated.boxentities.GenRegistry")
    .newInstance()
    .asInstanceOf[RegistryInstance]).toOption
}
