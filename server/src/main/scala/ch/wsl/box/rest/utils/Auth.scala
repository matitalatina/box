package ch.wsl.box.rest.utils

import java.security.MessageDigest
import java.util.Properties

import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpChallenges}
import akka.http.scaladsl.server.Directives.{complete, get, onSuccess}
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.directives.{AuthenticationDirective, AuthenticationResult, Credentials, SecurityDirectives}
import akka.http.scaladsl.server.directives.Credentials.Missing
import com.typesafe.config.{Config, ConfigFactory, ConfigObject, ConfigValue, ConfigValueFactory}
import net.ceedubs.ficus.Ficus._
import ch.wsl.box.jdbc.PostgresProfile.api._
import scribe.Logging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

/**
 * Created by andreaminetti on 16/02/16.
 */
object Auth extends Logging {


  //val executor = AsyncExecutor("public-executor",50,50,10000,50)

  val dbConf: Config = ConfigFactory.load().as[Config]("db")
  val dbPath = dbConf.as[String]("url")
  val dbPassword = dbConf.as[String]("password")
  val dbSchema = dbConf.as[String]("schema")

  val boxDbConf: Config = ConfigFactory.load().as[Config]("box.db")
  val boxDbPath = boxDbConf.as[String]("url")
  val boxDbPassword = boxDbConf.as[String]("password")
  val boxDbSchema = boxDbConf.as[String]("schema")

  println(s"DB: $dbPath")
  println(s"Box DB: $boxDbPath")

  /**
    * Admin DB connection, useful for quering the information Schema
 *
    * @return
    */

  val adminDB = Database.forConfig("",ConfigFactory.empty()
    .withValue("driver",ConfigValueFactory.fromAnyRef("org.postgresql.Driver"))
    .withValue("url",ConfigValueFactory.fromAnyRef(s"$dbPath?currentSchema=$dbSchema"))
    .withValue("keepAliveConnection",ConfigValueFactory.fromAnyRef(true))
    .withValue("user",ConfigValueFactory.fromAnyRef(dbConf.as[String]("user")))
    .withValue("password",ConfigValueFactory.fromAnyRef(dbConf.as[String]("password")))
    .withValue("numThreads",ConfigValueFactory.fromAnyRef(5))
    .withValue("maximumPoolSize",ConfigValueFactory.fromAnyRef(5))
  )

  val boxDB = Database.forConfig("",ConfigFactory.empty()
    .withValue("driver",ConfigValueFactory.fromAnyRef("org.postgresql.Driver"))
    .withValue("url",ConfigValueFactory.fromAnyRef(s"$boxDbPath?currentSchema=$boxDbSchema"))
    .withValue("keepAliveConnection",ConfigValueFactory.fromAnyRef(true))
    .withValue("user",ConfigValueFactory.fromAnyRef(boxDbConf.as[String]("user")))
    .withValue("password",ConfigValueFactory.fromAnyRef(boxDbConf.as[String]("password")))
    .withValue("numThreads",ConfigValueFactory.fromAnyRef(3))
    .withValue("maximumPoolSize",ConfigValueFactory.fromAnyRef(3))
  )

  def adminUserProfile = UserProfile(
    name=dbConf.as[String]("user"),
    db=adminDB,
    boxDb=boxDB
  )

  def boxUserProfile = UserProfile(
    name=boxDbConf.as[String]("user"),
    db=boxDB,
    boxDb=boxDB
  )





  val userProfiles:scala.collection.mutable.Map[String,UserProfile] = scala.collection.mutable.Map()

  /**
    * check if this is a valid user on your system and return his profile,
    * that include his username and the connection to the DB
    */
  def getUserProfile(name: String, password: String): UserProfile = {

    val hash = MessageDigest.getInstance("MD5").digest(s"$name $password".getBytes()).map(0xFF & _).map { "%02x".format(_) }.foldLeft("") {_ + _}

    userProfiles.get(hash) match {
      case Some(up) => up
      case None => {

        logger.info(s"Creating new pool for $name with hash $hash")

        val db = Database.forConfig("",ConfigFactory.empty()
          .withValue("driver",ConfigValueFactory.fromAnyRef("org.postgresql.Driver"))
          .withValue("url",ConfigValueFactory.fromAnyRef(s"$dbPath?currentSchema=$dbSchema"))
          .withValue("keepAliveConnection",ConfigValueFactory.fromAnyRef(true))
          .withValue("user",ConfigValueFactory.fromAnyRef(name))
          .withValue("password",ConfigValueFactory.fromAnyRef(password))
          .withValue("maximumPoolSize",ConfigValueFactory.fromAnyRef(3))
          .withValue("numThreads",ConfigValueFactory.fromAnyRef(3))
          .withValue("minimumIdle",ConfigValueFactory.fromAnyRef(0))
          .withValue("idleTimeout",ConfigValueFactory.fromAnyRef(10000))
        )

        val boxDb = Database.forConfig("",ConfigFactory.empty()
          .withValue("driver",ConfigValueFactory.fromAnyRef("org.postgresql.Driver"))
          .withValue("url",ConfigValueFactory.fromAnyRef(s"$boxDbPath?currentSchema=$boxDbSchema"))
          .withValue("keepAliveConnection",ConfigValueFactory.fromAnyRef(true))
          .withValue("user",ConfigValueFactory.fromAnyRef(name))
          .withValue("password",ConfigValueFactory.fromAnyRef(password))
          .withValue("maximumPoolSize",ConfigValueFactory.fromAnyRef(3))
          .withValue("numThreads",ConfigValueFactory.fromAnyRef(3))
          .withValue("minimumIdle",ConfigValueFactory.fromAnyRef(0))
          .withValue("idleTimeout",ConfigValueFactory.fromAnyRef(10000))
        )



        val up = UserProfile(name,db,boxDb)

        userProfiles += hash -> up

        up

      }
    }







  }

  //todo: verificare differenza di Auth.boxDB con userProfile.box
  def onlyAdminstrator(s:BoxSession)(r:Route)(implicit ec: ExecutionContext):Route = {

    onSuccess(s.userProfile.accessLevel){
      case 1000 => r
      case al => get {
        complete("You don't have the rights (access level = " + al + ")")
      }
    }

  }

  def userProfileForUser(u:String):UserProfile = {
    val prop = new Properties()
    prop.setProperty("connectionInitSql",s"SET ROLE $u")

    val confDb = ConfigFactory.empty()
      .withValue("driver",ConfigValueFactory.fromAnyRef("org.postgresql.Driver"))
      .withValue("connectionInitSql",ConfigValueFactory.fromAnyRef(s"SET ROLE $u"))
      .withValue("url",ConfigValueFactory.fromAnyRef(s"$dbPath?currentSchema=$dbSchema"))
      .withValue("keepAliveConnection",ConfigValueFactory.fromAnyRef(true))
      .withValue("user",ConfigValueFactory.fromAnyRef(dbConf.as[String]("user")))
      .withValue("password",ConfigValueFactory.fromAnyRef(dbConf.as[String]("password")))
      .withValue("maximumPoolSize",ConfigValueFactory.fromAnyRef(3))
      .withValue("numThreads",ConfigValueFactory.fromAnyRef(3))

    val confBoxDb = ConfigFactory.empty()
      .withValue("driver",ConfigValueFactory.fromAnyRef("org.postgresql.Driver"))
      .withValue("connectionInitSql",ConfigValueFactory.fromAnyRef(s"SET ROLE $u"))
      .withValue("url",ConfigValueFactory.fromAnyRef(s"$boxDbPath?currentSchema=$boxDbSchema"))
      .withValue("keepAliveConnection",ConfigValueFactory.fromAnyRef(true))
      .withValue("user",ConfigValueFactory.fromAnyRef(boxDbConf.as[String]("user")))
      .withValue("password",ConfigValueFactory.fromAnyRef(boxDbConf.as[String]("password")))
      .withValue("maximumPoolSize",ConfigValueFactory.fromAnyRef(1))
      .withValue("numThreads",ConfigValueFactory.fromAnyRef(1))


    val db = Database.forConfig("",confDb)
    val boxDB = Database.forConfig("",confBoxDb)

    UserProfile(
      name=u,
      db=db,
      boxDb=boxDB
    )

  }



}
