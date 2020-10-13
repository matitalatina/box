package ch.wsl.box.client

import ch.wsl.box.client.mocks.Values
import org.scalajs.dom.document
import org.scalajs.dom.ext._


class LoaderSpec extends BaseSpec  {

  val ready = load()

  "Index page" should "have the title defined in conf" in {
    ready.map{ _ =>
      document.querySelectorAll("#headerTitle").count(_.textContent == Values.uiConf("title")) shouldBe 1
    }
  }

}
