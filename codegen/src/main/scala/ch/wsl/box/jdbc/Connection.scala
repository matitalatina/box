package ch.wsl.box.jdbc

import java.security.MessageDigest

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import net.ceedubs.ficus.Ficus._
import scribe.Logging
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.{ResultSetConcurrency, ResultSetType}
import slick.sql.SqlAction
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.jdbc.UserDatabase

import scala.concurrent.{Await, ExecutionContext}

/**
  * Created by andreaminetti on 16/02/16.
  */
object Connection extends Logging {


  //val executor = AsyncExecutor("public-executor",50,50,10000,50)

  val dbConf: Config = ConfigFactory.load().as[Config]("db")
  val dbPath = dbConf.as[String]("url")
  val dbPassword = dbConf.as[String]("password")
  val dbSchema = dbConf.as[String]("schema")
  val adminPoolSize = dbConf.as[Option[Int]]("adminPoolSize").getOrElse(5)
  val poolSize = dbConf.as[Option[Int]]("poolSize").getOrElse(3)
  val enableConnectionPool = dbConf.as[Option[Boolean]]("enableConnectionPool").getOrElse(true)
  val adminUser = dbConf.as[String]("user")

  val connectionPool = if (enableConnectionPool) {
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

  val dbConnection = Database.forConfig("", ConfigFactory.empty()
    .withValue("driver", ConfigValueFactory.fromAnyRef("org.postgresql.Driver"))
    .withValue("url", ConfigValueFactory.fromAnyRef(dbPath))
    .withValue("keepAliveConnection", ConfigValueFactory.fromAnyRef(true))
    .withValue("user", ConfigValueFactory.fromAnyRef(adminUser))
    .withValue("password", ConfigValueFactory.fromAnyRef(dbConf.as[String]("password")))
    .withValue("numThreads", ConfigValueFactory.fromAnyRef(adminPoolSize))
    .withValue("maximumPoolSize", ConfigValueFactory.fromAnyRef(adminPoolSize))
    .withValue("connectionPool", connectionPool)
  )

  val adminDB = dbForUser(adminUser)




  def dbForUser(name: String): UserDatabase = new UserDatabase {

    //cannot interpolate directly
    val setRole: SqlAction[Int, NoStream, Effect] = sqlu"SET ROLE placeholder".overrideStatements(Seq(s"SET ROLE $name"))
    val resetRole = sqlu"RESET ROLE"

    override def stream[T](a: StreamingDBIO[Seq[T], T]) = {

      Connection.dbConnection.stream[T](
        setRole.andThen[Seq[T], Streaming[T], Nothing](a)
          .withStatementParameters(
            rsType = ResultSetType.ForwardOnly,
            rsConcurrency = ResultSetConcurrency.ReadOnly,
            fetchSize = 5000)
          .withPinnedSession
          .transactionally
      )


    }

    override def run[R](a: DBIOAction[R, NoStream, Nothing]) = {
      Connection.dbConnection.run {
        setRole.andThen[R, NoStream, Nothing](a).withPinnedSession.transactionally
      }
    }
  }


}
