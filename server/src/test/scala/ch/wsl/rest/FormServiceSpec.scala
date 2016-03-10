package ch.wsl.rest


import ch.wsl.rest.domain.JSONField
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner


@RunWith(classOf[JUnitRunner])
class FormServiceSpec extends BaseSpec {


  import ch.wsl.rest.service.JsonProtocol._

  "Form masks" should {

    "return the available form list" in {
      Get("/form") ~> withAuth ~> route ~> check {
        responseAs[Seq[String]] === Seq("test_form")
      }
    }

    "return a form selected by id" in {
      Get("/form/test_form") ~> withAuth ~> route ~> check {
        val form = responseAs[Seq[JSONField]]
        form(0).table === "a"
        form(0).key === "test"
      }
    }

  }

}