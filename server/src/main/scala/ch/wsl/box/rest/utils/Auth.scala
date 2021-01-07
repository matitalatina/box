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
import ch.wsl.box.jdbc.UserDatabase
import scribe.Logging
import slick.dbio.{DBIOAction, NoStream}
import slick.sql.SqlAction

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
  val adminPoolSize = dbConf.as[Option[Int]]("adminPoolSize").getOrElse(5)
  val poolSize = dbConf.as[Option[Int]]("poolSize").getOrElse(3)
  val enableConnectionPool = dbConf.as[Option[Boolean]]("enableConnectionPool").getOrElse(true)
  val adminUser = dbConf.as[String]("user")

  val connectionPool = if(enableConnectionPool) {
    ConfigValueFactory.fromAnyRef("HikariCP")
  } else {
    ConfigValueFactory.fromAnyRef("disabled")
  }



  println(s"DB: $dbPath")

  /**
    * Admin DB connection, useful for quering the information Schema
 *
    * @return
    */

  val dbConnection = Database.forConfig("",ConfigFactory.empty()
    .withValue("driver",ConfigValueFactory.fromAnyRef("org.postgresql.Driver"))
    .withValue("url",ConfigValueFactory.fromAnyRef(dbPath))
    .withValue("keepAliveConnection",ConfigValueFactory.fromAnyRef(true))
    .withValue("user",ConfigValueFactory.fromAnyRef(adminUser))
    .withValue("password",ConfigValueFactory.fromAnyRef(dbConf.as[String]("password")))
    .withValue("numThreads",ConfigValueFactory.fromAnyRef(adminPoolSize))
    .withValue("maximumPoolSize",ConfigValueFactory.fromAnyRef(adminPoolSize))
    .withValue("connectionPool",connectionPool)
  )

  val adminDB = dbForUser(adminUser)


  def adminUserProfile = UserProfile(
    name=dbConf.as[String]("user")
  )







  val userProfiles:scala.collection.mutable.Map[String,UserProfile] = scala.collection.mutable.Map()

  /**
    * check if this is a valid user on your system and return his profile,
    * that include his username and the connection to the DB
    */
  def getUserProfile(name: String, password: String)(implicit executionContext: ExecutionContext): Option[UserProfile] = {

    val hash = MessageDigest.getInstance("MD5").digest(s"$name $password".getBytes()).map(0xFF & _).map { "%02x".format(_) }.foldLeft("") {_ + _}

    userProfiles.get(hash).orElse{

        logger.info(s"Creating new pool for $name with hash $hash with poolsize $poolSize")


        val validUser = Await.result(Database.forURL(dbPath,name,password,driver = "org.postgresql.Driver").run{
          sql"""select 1""".as[Int]
        }.map{ _ =>
          true
        }.recover{case _ => false},2 seconds)

        if(validUser) {

          val up = UserProfile(name)

          userProfiles += hash -> up

          Some(up)
        } else {
          None
        }


    }

  }

  //todo: verificare differenza di Auth.boxDB con userProfile.box
  def onlyAdminstrator(s:BoxSession)(r:Route)(implicit ec: ExecutionContext):Route = {

    onSuccess(s.userProfile.get.accessLevel){
      case i:Int if i>=900 => r
      case al => get {
        complete("You don't have the rights (access level = " + al + ")")
      }
    }

  }

  def userProfileForUser(u:String):UserProfile = {

    UserProfile(
      name=u
    )
  }

  def dbForUser(name:String):UserDatabase = new UserDatabase {

      //cannot interpolate directly
      val setRole: SqlAction[Int, NoStream, Effect] = sqlu"SET ROLE placeholder".overrideStatements(Seq(s"SET ROLE $name"))
      val resetRole = sqlu"RESET ROLE"

      override def stream[T](a: DBIOAction[Seq[T], Streaming[T], Nothing]) = {

        Auth.dbConnection.stream[T](
          setRole.andThen[Seq[T],Streaming[T],Nothing](a).andFinally(resetRole)
        )


      }

      override def run[R](a: DBIOAction[R, NoStream, Nothing]) = {
        Auth.dbConnection.run {
          setRole.andThen[R,NoStream,Nothing](a).andFinally(resetRole)
        }
      }
    }



}
