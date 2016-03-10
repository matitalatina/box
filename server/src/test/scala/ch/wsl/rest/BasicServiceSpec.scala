package ch.wsl.rest


import ch.wsl.rest.service.{JsonProtocol, MainService}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import spray.http.BasicHttpCredentials
import spray.http.HttpHeaders.Authorization


@RunWith(classOf[JUnitRunner])
class BasicServiceSpec extends BaseSpec {


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


}