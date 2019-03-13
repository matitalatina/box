package ch.wsl.box.rest

import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures
import ch.wsl.box.rest.logic.{Context, RuntimeFunction, RuntimePSQL, RuntimeWS}
import io.circe.Json

import scala.concurrent.Future


class RuntimeFunctionSpec extends FlatSpec with ScalaFutures {


  val context = Context(
    Json.Null,
    new RuntimeWS {
      override def get(url: String): Future[String] = ???

      override def post(url: String, data: String): Future[String] = ???
    },
    new RuntimePSQL {
      override def function(name: String, parameters: Seq[Json]): Future[Seq[Seq[String]]] = ???
    }
    )

  "Function" should "be parsed and evaluated" in {

    val code =
      """
        |Future.successful(Seq(Seq("test")))
      """.stripMargin

    whenReady(RuntimeFunction(code)(context)) { result =>
      assert(result.head.head == "test")
    }
  }

}
