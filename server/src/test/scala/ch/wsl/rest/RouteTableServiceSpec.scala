package ch.wsl.rest

import ch.wsl.jsonmodels._
import ch.wsl.rest.domain._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner


import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.{read, write}

import ch.wsl.model.tablesTestDB._

@RunWith(classOf[JUnitRunner])
class RouteTableServiceSpec extends BaseSpec {


  import ch.wsl.rest.service.JsonProtocol._

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


  "A table json objects" should {




    "check A validity" in {
      read[ARow](jsonA).id === 1
    }

    "be empty" in {
      get(endpoint+"/a") {
        responseAs[List[ARow]].isEmpty
      }
    }

    "create a new row" in {
      post(endpoint + "/a", parse(jsonA)) {
        println(response.toString)
        handled must beTrue
      }
    }

    "not be empty" in {
      get(endpoint+"/a") {
        responseAs[List[ARow]].length > 0
      }
    }

    "return correct row" in {
      get(endpoint+"/a/"+expectedA.id) {
        responseAs[ARow] === expectedA
      }
    }

    "update a row" in {
      put(endpoint + "/a/"+expectedA.id,parse(jsonAmod)) {
        handled must beTrue
      }
    }

    "return updated row" in {
      get(endpoint+"/a/"+expectedA.id) {
        responseAs[ARow] === expectedAmod
      }
    }

    "count rows" in {
      get(endpoint+"/a/count") {
        responseAs[JSONCount].count === 1
      }
    }

    "list rows" in {
      post(endpoint + "/a/list",JSONQuery(10,1,Map(),Map()))  {
        responseAs[JSONResult[ARow]].count === 1
      }
    }

    "delete row" in {
      delete(endpoint + "/a/"+expectedA.id) {
        handled must beTrue
        responseAs[JSONCount].count === 1
      }
    }

    "be empty at the end" in {
      get(endpoint+"/a") {
        responseAs[List[ARow]].isEmpty
      }
    }

  }

  "UI Definition JSON" should {

    "list a keys" in {
      get(endpoint+"/a/keys")  {
        responseAs[Seq[String]] === Seq("id")
      }
    }


    "create a valid JSONSchema for a table" in {
      get(endpoint+"/a/schema")  {
        val schema = responseAs[JSONSchema]
        schema.title === Some("a")
        schema.`type` === "object"
        schema.properties.toList.map(x => (x._1,x._2.`type`)).diff(
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
      get(endpoint+"/a/form")   {
        val form = responseAs[List[JSONField]]
        form.map(_.key) === List("id","string1","string2","short","integer","double","double2","long")
      }
    }


  }

}