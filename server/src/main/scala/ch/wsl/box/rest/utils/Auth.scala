package ch.wsl.box.rest.utils

import java.security.MessageDigest

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.wsl.box.jdbc.Connection.{dbConf, dbPath, logger, poolSize}
import net.ceedubs.ficus.Ficus._
import ch.wsl.box.jdbc.PostgresProfile.api._
import scribe.Logging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

object Auth extends Logging {


  def adminUserProfile = UserProfile(
    name = dbConf.as[String]("user")
  )


  val userProfiles: scala.collection.mutable.Map[String, UserProfile] = scala.collection.mutable.Map()

  /**
    * check if this is a valid user on your system and return his profile,
    * that include his username and the connection to the DB
    */
  def getUserProfile(name: String, password: String)(implicit executionContext: ExecutionContext): Option[UserProfile] = {

    val hash = MessageDigest.getInstance("MD5").digest(s"$name $password".getBytes()).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }

    userProfiles.get(hash).orElse {

      logger.info(s"Creating new pool for $name with hash $hash with poolsize $poolSize")


      val validUser = Await.result(Database.forURL(dbPath, name, password, driver = "org.postgresql.Driver").run {
        sql"""select 1""".as[Int]
      }.map { _ =>
        true
      }.recover { case _ => false }, 2 seconds)

      if (validUser) {

        val up = UserProfile(name)

        userProfiles += hash -> up

        Some(up)
      } else {
        None
      }


    }

  }

  //todo: verificare differenza di Auth.boxDB con userProfile.box
  def onlyAdminstrator(s: BoxSession)(r: Route)(implicit ec: ExecutionContext): Route = {

    onSuccess(s.userProfile.get.accessLevel) {
      case i: Int if i >= 900 => r
      case al => get {
        complete("You don't have the rights (access level = " + al + ")")
      }
    }

  }

  def userProfileForUser(u: String): UserProfile = {

    UserProfile(
      name = u
    )
  }

}
