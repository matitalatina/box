package ch.wsl.rest.domain

import scala.slick.jdbc.JdbcBackend.Database

trait DBConfig {
  def db: Database
}

import scala.slick.driver.PostgresDriver

trait ProductionDB extends DBConfig {
  val db = Database.forURL("jdbc:postgresql:incendi",
                           driver="org.postgresql.Driver",
                           user="tree",
                           password="tree")
}
