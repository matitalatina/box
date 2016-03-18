package ch.wsl.box.rest.service

import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import slick.driver.PostgresDriver.api._
import spray.routing.authentication._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
 * Created by andreaminetti on 16/02/16.
 */
object Auth {



  val dbConf: Config = ConfigFactory.load().as[Config]("db")
  val dbPath = dbConf.as[String]("url")

  /**
    * Admin DB connection, useful for quering the information Schema
 *
    * @return
    */
  def adminDB = Database.forURL(dbPath,
    driver="org.postgresql.Driver",
    user=dbConf.as[String]("user"),
    password=dbConf.as[String]("password"))


  case class UserProfile(name: String, db: Database)

  /**
    * check if this is a valid user on your system and return his profile,
    * that include his username and the connection to the DB
    */
  def getUserProfile(name: String, password: String): Option[UserProfile] = {


    println(s"Connecting to DB $dbPath with $name")

    val result = Try {

      val db:Database = Database.forURL(dbConf.as[String]("url"),
        driver="org.postgresql.Driver",
        user=name,
        password=password)

      Await.result(db.run{
        sql"""select 1""".as[Int]
      },1 second)

      db

    }.toOption.map{ db =>
      UserProfile(name,db)
    }

    println(result)


    result

  }

  /**
    * #Custom Authenticator
    *
    * It authenticate the users against PostgresSQL roles
    *
    */
  object CustomUserPassAuthenticator extends UserPassAuthenticator[UserProfile] {
    def apply(userPass: Option[UserPass]) =
      userPass match {
        case Some(UserPass(user, pass)) =>  Future.successful(getUserProfile(user, pass))
        case _ => Future.successful(None)
      }
  }

}
