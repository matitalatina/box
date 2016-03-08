package ch.wsl.rest

import ch.wsl.rest.service.{JsonProtocol, MainService}
import org.junit.runner.RunWith
import org.specs2.Specification
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
class ServiceSpec extends Specification with Specs2RouteTest with MainService {
  def actorRefFactory = system

  def is = {
    var string2NR = () // shadow implicit conversion from Spray Directives trait
    sequential ^
      p ^
      "Postgres REST Specification" ^
      p ^
      "The server should" ^
      "Respond with greeting on root path" ! serverRunning ^
      p ^
      "Check authentication negatives" ^
      "Require authentication - No username and password" ! requireAuthentication ^
      "Fail authentication - Wrong username or password" ! failAuthentication ^
      p ^
      "Check authentication positive" ^
      "Require authentication - Correct username and password" ! okAuthentication ^
      p ^
      "For A table json objects" ^
      "Return an empty list if there are no entities" ! getEmptyAList ^
      "Check the json is a valid A entity" ! checkA ^
      "Create a new entity" ! createA ^
      "Return a non-empty list if there some entities" ! getNonEmptyAList ^
      "Read existing" ! todo ^
      "Update existing" ! todo ^
      "Delete existing" ! todo ^
      "Handle missing fields" ! todo ^
      "Handle invalid fields" ! todo ^
      "Return error if the entity does not exist" ! todo ^
      end
  }

  def serverRunning = Get("/") ~> route ~> check {
    responseAs[String] must contain("REST")
  }

  import JsonProtocol._

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

  def checkA = {
    read[ARow](jsonA)
    ok
  }

  val withAuth = addHeader(Authorization(BasicHttpCredentials("andreaminetti", "")))

  def getEmptyAList = {
    Get("/a") ~> withAuth ~> route ~> check {
      responseAs[List[ARow]] === List()
      ok
    }
  }

  def getNonEmptyAList = {
    Get("/a") ~> withAuth ~> route ~> check {
      responseAs[List[ARow]].length > 0
      ok
    }
  }

  def createA = {
    Post("/a", parse(jsonA)) ~> withAuth ~> route ~> check {
      println(response.toString)
      ok
    }
  }

  def failAuthentication = {
    Get("/a") ~> addHeader(Authorization(BasicHttpCredentials("boob", "111123"))) ~> route ~> check {
      handled must beFalse
    //  rejection must beAnInstanceOf[AuthenticationFailedRejection]
    }
  }

  def requireAuthentication = {
    Get("/a") ~> route ~> check {
      handled must beFalse
    //  rejection must beAnInstanceOf[AuthenticationFailedRejection]
    }
  }

  def okAuthentication = {
    Get("/a") ~> withAuth ~> route ~> check {
      handled must beTrue
      //  rejection must beAnInstanceOf[AuthenticationFailedRejection]
    }
  }
}