package ch.wsl.rest.service

import com.typesafe.config.{ConfigFactory, Config}
import spray.routing.authentication._

import scala.concurrent.Promise
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

  def getUserProfile(name: String, password: String): Option[UserProfile] = {
    //TODO Here you should check if this is a valid user on your system and return his profile

    val dbConf: Config = ConfigFactory.load().as[Config]("db")

    println("Connecting to DB with " + name )


    val db:Database = Database.forURL(dbConf.as[String]("url"),
      driver="org.postgresql.Driver",
      user=name,
      password=password)

    Try {
      //check if login data are valid
      db.createSession().close()
    }.toOption.map{ _ =>
      UserProfile(name,db)
    }

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
