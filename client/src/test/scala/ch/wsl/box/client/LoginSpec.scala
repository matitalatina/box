package ch.wsl.box.client

import ch.wsl.box.client.mocks.Values
import ch.wsl.box.client.services.ClientSession
import ch.wsl.box.client.utils.TestHooks
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.window
import org.scalajs.dom.ext._
import org.scalajs.dom.raw.HTMLElement

import scala.scalajs.js
import scala.scalajs.js.{Promise, Thenable, |}




class LoginSpec extends BaseSpec {



  val ready = for{
    _ <- load()
  } yield true



  "Login" should "be done" in {
    ready.flatMap { _ =>
      println(dom.window.sessionStorage.getItem(ClientSession.USER))
      val beforeLogin = document.body.innerHTML
      document.querySelectorAll(s"#${TestHooks.logoutButton}").length shouldBe 0
      Context.services.clientSession.login("test","test").map { x =>
        println(dom.window.sessionStorage.getItem(ClientSession.USER))
        beforeLogin shouldNot be (document.body.innerHTML)
        println(document.body.innerHTML)
        println(window.location.href)
        document.querySelectorAll(s"#${TestHooks.logoutButton}").length shouldBe 1
        window.location.href.contains("home") shouldBe true
      }
    }
  }
}
