package ch.wsl.rest.domain

import scala.slick.jdbc.JdbcBackend.Database


import scala.slick.driver.PostgresDriver


import com.typesafe.config._
import net.ceedubs.ficus.Ficus._


trait DBConfig {
  val profile = scala.slick.driver.PostgresDriver
}

object DBConfig {
  
  val dbConf: Config = ConfigFactory.load().as[Config]("db")
  
  def db = Database.forURL(dbConf.as[String]("url"),
                           driver="org.postgresql.Driver",
                           user=dbConf.as[String]("user"),
                           password=dbConf.as[String]("password"))
}
