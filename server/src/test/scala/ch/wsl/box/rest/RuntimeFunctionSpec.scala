package ch.wsl.box.rest

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import ch.wsl.box.rest.jdbc.PostgresProfile
import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import ch.wsl.box.rest.logic._
import ch.wsl.box.rest.utils.UserProfile
import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}


class RuntimeFunctionSpec extends FlatSpec with ScalaFutures {

  import ch.wsl.box.rest.jdbc.PostgresProfile.api._
  import scala.concurrent.ExecutionContext.Implicits.global


  implicit val db = Database.forURL("jdbc:postgresql:box_test", "postgres", "password", driver="org.postgresql.Driver")
  implicit val up  = UserProfile("a",db,db)

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val dr = DataResult(Seq("aa"),Seq(Seq("aa","bb")))

  val context = Context(
    Json.Null,
    new RuntimeWS {
      override def get(url: String): Future[String] = ???

      override def post(url: String, data: String): Future[String] = ???
    },
    new RuntimePSQL {
      override def function(name: String, parameters: Seq[Json], lang:String)(implicit ec:ExecutionContext,db:Database): Future[Option[DataResult]] = {
        Future.successful(Some(dr))
      }

      override def table(name: String, lang: String)(implicit ec: ExecutionContext, up: UserProfile, mat: Materializer): Future[Option[DataResult]] = ???
    }
    )

  "Function" should "be parsed and evaluated" in {

    val code =
      """
        |Future.successful(DataResult(Seq(),Seq(Seq("test"))))
      """.stripMargin
    val f = RuntimeFunction("test1",code)
    whenReady(f(context)) { result =>
      assert(result.rows.head.head == "test")
    }
  }

  "Function" should "with external call should be parsed and evaluated" in {

    val code =
      """
        |context.psql.function("",Seq(),"").map(_.get)
      """.stripMargin
    val f = RuntimeFunction("test2",code)
    whenReady(f(context)) { result =>
      assert(result == dr)
    }
  }

}
