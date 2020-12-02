package ch.wsl.box.client

import ch.wsl.box.client.mocks.Values
import ch.wsl.box.client.services.{ClientConf, ClientSession, Labels}
import org.scalajs.dom.{document, window}
import org.scalajs.dom.ext._
import utest.{Tests, assert, test}

object LanguageOrderTest extends TestBase {

  import Context._

  window.sessionStorage.setItem(ClientSession.LANG, "it")

  def checkOnHeader(ref:String) = document.querySelectorAll("header").forall { x =>
    x.textContent.contains(ref)
  }

  val tests = Tests{
    test("language order test") - {
      Main.setupUI().map { _ =>
        assert(ClientConf.langs.length == 2)
        assert(ClientConf.langs(0) == "it")
        assert(ClientConf.langs(1) == "en")
      }
    }
  }

}