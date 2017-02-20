//package ch.wsl.box.rest
//
//
//class BasicServiceSpec extends BaseSpec {
//
//
//  "The service" should {
//
//    "Respond with greeting on root path" in {
//      Get("/") ~> route ~> check {
//        response.toString must contain("Postgres-UI")
//      }
//    }
//
//  }
//
//  "Check authentication negatives" should {
//
//    "Require authentication - No username and password" in {
//      Get(endpoint + "/a") ~> route ~> check {
//        handled must beFalse
//      }
//    }
//
//    "Fail authentication - Wrong username or password" in {
//      Get(endpoint + "/a") ~> addHeader(Authorization(BasicHttpCredentials("boob", "111123"))) ~> route ~> check {
//        handled must beFalse
//      }
//    }
//  }
//
//  "Check authentication positive" in {
//    "Require authentication - Correct username and password"  in {
//      get(endpoint + "/a") {
//        println(response.toString)
//        handled must beTrue
//      }
//    }
//  }
//
//
//}