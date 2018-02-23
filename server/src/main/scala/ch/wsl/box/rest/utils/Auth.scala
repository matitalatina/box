package ch.wsl.box.rest.utils

import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpChallenges}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.{AuthenticationDirective, AuthenticationResult, Credentials, SecurityDirectives}
import akka.http.scaladsl.server.directives.Credentials.Missing
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import slick.driver.PostgresDriver.api._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
 * Created by andreaminetti on 16/02/16.
 */
object Auth {


  val executor = AsyncExecutor("public-executor",10,10,1000)

  val dbConf: Config = ConfigFactory.load().as[Config]("db")
  val dbPath = dbConf.as[String]("url")
  val dbSchema = dbConf.as[String]("schema")

  val boxDbConf: Config = ConfigFactory.load().as[Config]("box.db")
  val boxDbPath = boxDbConf.as[String]("url")
  val boxDbSchema = boxDbConf.as[String]("schema")
  /**
    * Admin DB connection, useful for quering the information Schema
 *
    * @return
    */
  def adminDB = Database.forURL(s"$dbPath?currentSchema=$dbSchema",
    driver="org.postgresql.Driver",
    user=dbConf.as[String]("user"),
    password=dbConf.as[String]("password"),
    executor = executor
  )

  def boxDB = Database.forURL(s"$boxDbPath?currentSchema=$boxDbSchema",
    driver="org.postgresql.Driver",
    user=boxDbConf.as[String]("user"),
    password=boxDbConf.as[String]("password"),
    executor = executor
  )


  /**
    * check if this is a valid user on your system and return his profile,
    * that include his username and the connection to the DB
    */
  def getUserProfile(name: String, password: String): UserProfile = {


//    println(s"Connecting to DB $dbPath with $name")


      val db:Database = Database.forURL(s"$dbPath?currentSchema=$dbSchema",
        driver="org.postgresql.Driver",
        user=name,
        password=password,
        executor = executor)

      val boxDb:Database = Database.forURL(s"$boxDbPath?currentSchema=$boxDbSchema",
      driver="org.postgresql.Driver",
      user=name,
      password=password,
      executor = executor)


      UserProfile(name,db,boxDb)




  }


}
