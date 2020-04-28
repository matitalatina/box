package ch.wsl.box.rest

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import ch.wsl.box.rest.utils.{BoxConf, UserProfile}
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.runtime.Registry
import ch.wsl.box.testmodel.GenRegistry
import scribe.{Level, Logger}


trait BaseSpec extends FlatSpec with ScalaFutures with Matchers {

  private val executor = AsyncExecutor("public-executor",50,50,1000,50)

  Logger.root.clearHandlers().withHandler(minimumLevel = Some(Level.Warn)).replace()
  //Logger.select(className("scala.slick")).setLevel(Level.Debug)

  implicit override val patienceConfig = PatienceConfig(timeout = 10.seconds)

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val dbPath = System.getenv("TEST_DB_URL")
  val dbSchema = "public"
  val dbUsername = "postgres"
  val dbPassword = System.getenv("TEST_DB_PASSWORD")

  implicit val db = Database.forURL(s"$dbPath?currentSchema=$dbSchema",
    driver="org.postgresql.Driver",
    user=dbUsername,
    password=dbPassword,
    executor = executor
  )
  implicit val up  = UserProfile(dbUsername,db,db)

  Registry.set(new GenRegistry())

}
