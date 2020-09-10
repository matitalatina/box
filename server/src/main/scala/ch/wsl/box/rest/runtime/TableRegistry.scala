package ch.wsl.box.rest.runtime

import akka.stream.Materializer
import ch.wsl.box.jdbc.PostgresProfile.api._

import scribe.Logging


trait TableRegistryEntry extends Logging {

  type MT

  def name:String

  def tableQuery: TableQuery[Table[MT]]
}

trait TableRegistry {
  def table(name:String):TableRegistryEntry
}
