package ch.wsl.rest

import akka.actor.ActorSystem
import ch.wsl.rest.domain._
import org.specs2.time.NoTimeConversions
import spray.testkit._
import concurrent.duration._
import akka.testkit._
import ch.wsl.rest.service.{JsonProtocol, MainService}
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import spray.http.{ContentType, HttpEntity, BasicHttpCredentials}
import spray.http.HttpHeaders.Authorization
import spray.http.MediaTypes.{ `application/json` }
import spray.http.HttpCharsets.{ `UTF-8` }
import spray.testkit.Specs2RouteTest

import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{read, write}

import ch.wsl.model.tablesTestDB._

@RunWith(classOf[JUnitRunner])
class ServiceSpec extends Specification with Specs2RouteTest with MainService with NoTimeConversions {
  // Set sequential execution
  sequential

  def actorRefFactory = system

  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(new DurationInt(20).second.dilated(system))


  import JsonProtocol._


  val withAuth = addHeader(Authorization(BasicHttpCredentials("andreaminetti", "")))


  "The service" should {

    "Respond with greeting on root path" in {
      Get("/") ~> route ~> check {
        response.toString must contain("REST")
      }
    }

  }

  "Check authentication negatives" should {

    "Require authentication - No username and password" in {
      Get("/a") ~> route ~> check {
        handled must beFalse
      }
    }

    "Fail authentication - Wrong username or password" in {
      Get("/a") ~> addHeader(Authorization(BasicHttpCredentials("boob", "111123"))) ~> route ~> check {
        handled must beFalse
      }
    }
  }

  "Check authentication positive" in {
    "Require authentication - Correct username and password"  in {
      Get("/a") ~> withAuth ~> route ~> check {
        println(response.toString)
        handled must beTrue
      }
    }
  }


  "A table json objects" should {

    sequential

    //preparation
    val jsonA = """{ "id": 1,
      "string1": "a",
      "string2": "b",
      "short": 1,
      "integer": 2,
      "double": 3,
      "double2": 4,
      "long": 5
      }"""



    val expectedA = ARow(
      id = 1,
      string1 = Some("a"),
      string2 = Some("b"),
      short = Some(1),
      integer = Some(2),
      double = Some(3),
      double2 = Some(4),
      long = Some(5)
    )

    val expectedAmod = expectedA.copy(double = Some(6))
    val jsonAmod = jsonA.replaceAll("3","6")

    "check A validity" in {
      read[ARow](jsonA).id === 1
    }

    "be empty" in {
      Get("/a") ~> withAuth ~> route ~> check {
        responseAs[List[ARow]].isEmpty
      }
    }

    "create a new row" in {
      Post("/a", parse(jsonA)) ~> withAuth ~> route ~> check {
        println(response.toString)
        handled must beTrue
      }
    }

    "not be empty" in {
      Get("/a") ~> withAuth ~> route ~> check {
        responseAs[List[ARow]].length > 0
      }
    }

    "return correct row" in {
      Get("/a/"+expectedA.id) ~> withAuth ~> route ~> check {
        responseAs[ARow] === expectedA
      }
    }

    "update a row" in {
      Put("/a/"+expectedA.id,parse(jsonAmod)) ~> withAuth ~> route ~> check {
        handled must beTrue
      }
    }

    "return updated row" in {
      Get("/a/"+expectedA.id) ~> withAuth ~> route ~> check {
        responseAs[ARow] === expectedAmod
      }
    }

    "count rows" in {
      Get("/a/count") ~> withAuth ~> route ~> check {
        responseAs[JSONCount].count === 1
      }
    }

    "list rows" in {
      Post("/a/list",JSONQuery(10,1,Map(),Map())) ~> withAuth ~> route ~> check {
        responseAs[JSONResult[ARow]].count === 1
      }
    }

    "delete row" in {
      Delete("/a/"+expectedA.id) ~> withAuth ~> route ~> check {
        handled must beTrue
        responseAs[JSONCount].count === 1
      }
    }

    "be empty at the end" in {
      Get("/a") ~> withAuth ~> route ~> check {
        responseAs[List[ARow]].isEmpty
      }
    }

  }

  "UI Definition JSON" should {

    "list a keys" in {
      Get("/a/keys") ~> withAuth ~> route ~> check {
        responseAs[Seq[String]] === Seq("id")
      }
    }


    "create a valid JSONSchema for a table" in {
      Get("/a/schema") ~> withAuth ~> route ~> check {
        val schema = responseAs[JSONSchema]
        schema.title === Some("a")
        schema.`type` === "object"
        schema.properties.toList.flatMap(_.map(x => (x._1,x._2.`type`))).diff(
          List(
            ("id","number"),
            ("string1","string"),
            ("string2","string"),
            ("short","number"),
            ("integer","number"),
            ("double","number"),
            ("double2","number"),
            ("long","number")
          )
        ).isEmpty
      }
    }

    "create a valid default UI form json" in {
      Get("/a/form") ~> withAuth ~> route ~> check {
        val form = responseAs[List[JSONField]]
        form.map(_.key) === List("id","string1","string2","short","integer","double","double2","long")
      }
    }

    "save a form definition for table a" in todo

    "get a custom form definition for table a" in todo

  }

}