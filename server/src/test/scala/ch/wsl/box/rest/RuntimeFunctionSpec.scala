package ch.wsl.box.rest

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import ch.wsl.box.model.shared.JSONQuery

import ch.wsl.box.rest.logic._
import ch.wsl.box.rest.logic.functions.{Context, RuntimeFunction, RuntimePSQL, RuntimeWS}
import ch.wsl.box.rest.utils.{Lang, UserProfile}
import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}

class RuntimeFunctionSpec extends BaseSpec {

  import ch.wsl.box.jdbc.PostgresProfile.api._


  val dr = DataResultTable(Seq("aa"),Seq(Seq("aa","bb")))

  val context = Context(
    Json.Null,
    new RuntimeWS {
      override def get(url: String)(implicit ec: ExecutionContext, mat: Materializer, system: ActorSystem): Future[String] = ???

      override def post(url: String, data: String, contentType: String)(implicit ec: ExecutionContext, mat: Materializer, system: ActorSystem): Future[String] = ???
    },
    new RuntimePSQL {
      override def function(name:String,parameters:Seq[Json])(implicit lang:Lang, ec:ExecutionContext,db:Database): Future[Option[DataResultTable]] = {
        Future.successful(Some(dr))
      }

      def table(name:String, query:JSONQuery = JSONQuery.empty)(implicit lang:Lang, ec:ExecutionContext, up:UserProfile, mat:Materializer):Future[Option[DataResultTable]] = ???
    }
    )

  "Function" should "be parsed and evaluated" in withUserProfile { implicit up =>

    val code =
      """
        |Future.successful(DataResultTable(Seq(),Seq(Seq("test"))))
      """.stripMargin
    val f = RuntimeFunction("test1",code)
    f(context,"en").map{ result =>
      assert(result.asInstanceOf[DataResultTable].rows.head.head == "test")
    }
  }

  "Function" should "with external call should be parsed and evaluated" in withUserProfile { implicit up =>

    val code =
      """
        |context.psql.function("",Seq()).map(_.get)
      """.stripMargin
    val f = RuntimeFunction("test2",code)
    f(context,"en").map{ result =>
      assert(result == dr)
    }
  }


  "Function" should "with ws call should be parsed and evaluated" in withUserProfile { implicit up =>

    val code =
      """
        |for{
        |  result <- context.ws.get("http://wavein.ch")
        |} yield DataResultTable(Seq(result),Seq())
      """.stripMargin
    val f = RuntimeFunction("test3",code)
    f(RuntimeFunction.context(Json.Null),"en").map{ result =>
      assert(result.asInstanceOf[DataResultTable].headers.nonEmpty)
    }
  }




  "Function" should "do a POST call as well" in withUserProfile { implicit up =>
    val code =
      """
        |for{
        |  result <- context.ws.post("https://postman-echo.com/post","data","application/x-www-form-urlencoded; charset=UTF-8")
        |} yield DataResultTable(Seq(result),Seq())
      """.stripMargin
    val f = RuntimeFunction("test4",code)
    f(RuntimeFunction.context(Json.Null),"en").map{ result =>
      assert(result.asInstanceOf[DataResultTable].headers.head.contains("data"))
    }
  }

}
