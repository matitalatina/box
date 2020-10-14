package ch.wsl.box.client

import ch.wsl.box.client.mocks.Values
import utest._
import org.scalajs.dom.document
import org.scalajs.dom.ext._

object LoaderTest extends TestSuite with TestBase {

  import Context._

  val tests = Tests{
    test("loader test") - {
      Main.setupUI().map { _ =>
          assert(document.querySelectorAll("#headerTitle").count(_.textContent == Values.uiConf("title")) == 1)
      }
    }
  }

}