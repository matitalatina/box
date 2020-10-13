package ch.wsl.box.client

import ch.wsl.box.client.Context.services
import ch.wsl.box.client.mocks.Values
import ch.wsl.box.client.services.{ClientConf, ClientSession, Labels}
import ch.wsl.box.client.utils.TestHooks
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.window
import org.scalajs.dom.ext._
import org.scalajs.dom.raw.HTMLElement

class LabelsSpec extends BaseSpec {

  dom.window.sessionStorage.setItem(ClientSession.LANG, "it")

  val ready = load()


  def checkOnHeader(ref:String) = document.querySelectorAll("header").forall { x =>
    x.textContent.contains(ref)
  }

  "Lang switch" should "change lang in session" in {

    ready.flatMap { _ =>

      ClientConf.langs.length shouldBe 2


      services.clientSession.lang() shouldBe "it"
      checkOnHeader(Values.headerLangIt) shouldBe true

      services.clientSession.setLang("en").flatMap { _ =>
        services.clientSession.lang() shouldBe "en"
        Labels.header.lang shouldBe Values.headerLangEn
        checkOnHeader(Values.headerLangEn) shouldBe true

        services.clientSession.setLang("it").map { _ =>
          dom.window.sessionStorage.getItem(ClientSession.LANG) shouldBe "it"
          services.clientSession.lang() shouldBe "it"
          checkOnHeader(Values.headerLangIt) shouldBe true
          Labels.header.lang shouldBe Values.headerLangIt
        }
      }
    }


  }


}
