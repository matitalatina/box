package ch.wsl.box.rest.service

import akka.http.scaladsl.model.headers.{HttpChallenges, BasicHttpCredentials}
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.directives.{AuthenticationResult, SecurityDirectives, Credentials}
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
  object PostgresAuthenticator {

    import SecurityDirectives._

    def postgresBasicAuth = authenticateOrRejectWithChallenge[BasicHttpCredentials, UserProfile] { cred =>
      cred.flatMap(c => getUserProfile(c.username, c.password)) match {
        case Some(u) =>  Future.successful(AuthenticationResult.success(u))
        case None => Future.successful(AuthenticationResult.failWithChallenge(HttpChallenges.basic("Postgres user password")))
      }
    }

  }

}
