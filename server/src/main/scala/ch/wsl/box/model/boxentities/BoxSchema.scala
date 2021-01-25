package ch.wsl.box.model.boxentities

import com.typesafe.config.{Config, ConfigFactory, ConfigObject, ConfigValue, ConfigValueFactory}
import net.ceedubs.ficus.Ficus._

object BoxSchema {
  val schema = Some( ConfigFactory.load().as[String]("box.db.schema")) //using option because slick schema is optional
}
