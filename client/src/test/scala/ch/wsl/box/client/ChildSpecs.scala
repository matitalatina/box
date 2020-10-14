package ch.wsl.box.client

import ch.wsl.box.client.mocks.Values
import ch.wsl.box.client.services.ClientSession
import ch.wsl.box.client.utils.TestHooks
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.window
import org.scalajs.dom.ext._
import org.scalajs.dom.raw.HTMLElement
import org.scalatest.Assertion

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.util.Try

class ChildSpecs extends BaseSpec {
  val ready = for{
    _ <- load()
    _ <- Context.services.clientSession.login("test","test").flatMap(x => Future(x))
    _ <- Future { // running as feature so the operation get inserted in the running queue and when the next future is added this will be done
      Context.applicationInstance.goTo(EntityFormState("form", Values.testFormName, "true", None))
    }
  } yield true


  "Form with childs" should "be rendered" in {
    ready.flatMap { _ =>
        println(document.body.innerHTML)
        println(dom.window.sessionStorage.getItem(ClientSession.USER))
        Context.services.clientSession.isValidSession().map { x =>
          println(x)
          document.querySelectorAll("h3 span").count(_.textContent.contains(Values.testFormTitle)) shouldBe 1
          document.getElementById(TestHooks.tableChildId(2)) shouldBe a[HTMLElement]
        }
    }
  }

}
