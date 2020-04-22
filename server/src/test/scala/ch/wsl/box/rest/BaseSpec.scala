package ch.wsl.box.rest

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import ch.wsl.box.rest.utils.UserProfile
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.runtime.Registry
import ch.wsl.box.testmodel.GenRegistry
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._

trait BaseSpec extends FlatSpec with ScalaFutures with Matchers {

  private val executor = AsyncExecutor("public-executor",50,50,1000,50)

  implicit override val patienceConfig = PatienceConfig(timeout = 10.seconds)

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val dbConf: Config = ConfigFactory.load("test").as[Config]("db")
  println(dbConf)
  val dbPath = dbConf.as[String]("url")
  val dbSchema = dbConf.as[String]("schema")
  val dbUsername = dbConf.as[String]("user")
  val dbPassword = dbConf.as[String]("password")

  implicit val db = Database.forURL(s"$dbPath?currentSchema=$dbSchema",
    driver="org.postgresql.Driver",
    user=dbUsername,
    password=dbPassword,
    executor = executor
  )
  implicit val up  = UserProfile(dbUsername,db,db)

  Registry.set(new GenRegistry())

}
