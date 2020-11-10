package ch.wsl.box.rest

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import ch.wsl.box.rest.utils.{BoxConfig, UserProfile}
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.duration._
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.rest.runtime.Registry
import ch.wsl.box.testmodel.GenRegistry
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.PostgreSQLContainer
import org.scalatest.flatspec.{AnyFlatSpec, AsyncFlatSpec}
import org.scalatest.matchers.should.Matchers
import scribe.{Level, Logger}


trait BaseSpec extends AsyncFlatSpec with Matchers with TestContainerForAll {

  private val executor = AsyncExecutor("public-executor",50,50,1000,50)

  Logger.root.clearHandlers().withHandler(minimumLevel = Some(Level.Warn)).replace()
  //Logger.select(className("scala.slick")).setLevel(Level.Debug)

  override val containerDef = PostgreSQLContainer.Def(
    mountPostgresDataToTmpfs = true
  )

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()





  private def connectToContainerDB(container:PostgreSQLContainer, schema:String = "public"):Database = {

    val dbPath = container.jdbcUrl
    val dbUsername = container.username
    val dbPassword = container.password


    Database.forURL(s"$dbPath&currentSchema=$schema",
      driver="org.postgresql.Driver",
      user=dbUsername,
      password=dbPassword,
      executor = executor
    )
  }

  private def createUserProfile(container:PostgreSQLContainer)  = UserProfile(container.username,connectToContainerDB(container),connectToContainerDB(container,"box"))


  def withDB[A](runTest: Database => A): A = withContainers{ container =>
    val db = connectToContainerDB(container)
    runTest(db)

  }

  def withUserProfile[A](runTest: UserProfile => A): A = withContainers{ container =>
    runTest(createUserProfile(container))
  }




  Registry.set(new GenRegistry())

}
