package ch.wsl.rest.service

import com.typesafe.config.{ConfigFactory, Config}
import spray.routing.authentication._

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._
import scala.util.Try
import slick.driver.PostgresDriver.api._

import com.typesafe.config._
import net.ceedubs.ficus.Ficus._

/**
 * Created by andreaminetti on 16/02/16.
 */
object Auth {

  //TODO Extend UserProfile class depending on project requirements
  case class UserProfile(name: String, db: Database)


  val dbConf: Config = ConfigFactory.load().as[Config]("db")

  def adminDB = Database.forURL(dbConf.as[String]("url"),
    driver="org.postgresql.Driver",
    user=dbConf.as[String]("user"),
    password=dbConf.as[String]("password"))

  def getUserProfile(name: String, password: String): Option[UserProfile] = {
    //TODO Here you should check if this is a valid user on your system and return his profile



    println("Connecting to DB with " + name )

    val result = Try {
      val db:Database = Database.forURL(dbConf.as[String]("url"),
        driver="org.postgresql.Driver",
        user=name,
        password=password)
      //check if login data are valid
      //def select:DBIO[Int] =
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

  object CustomUserPassAuthenticator extends UserPassAuthenticator[UserProfile] {
    def apply(userPass: Option[UserPass]) = Promise.successful(
      userPass match {
        case Some(UserPass(user, pass)) => {
          getUserProfile(user, pass)
        }
        case _ => None
      }).future
  }

}
