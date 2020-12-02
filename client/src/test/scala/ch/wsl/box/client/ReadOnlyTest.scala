package ch.wsl.box.client

import ch.wsl.box.client.ChildTest.{countChilds, waitCycle}
import ch.wsl.box.client.mocks.Values
import ch.wsl.box.client.utils.TestHooks
import ch.wsl.box.model.shared.{EntityKind, JSONID, JSONKeyValue, SharedLabels}
import org.scalajs.dom.raw.{Event, HTMLElement, HTMLInputElement}
import org.scalajs.dom.{document, window}
import utest.{TestSuite, Tests, assert, test}

import scala.concurrent.Future

object ReadOnlyTest extends TestBase {

  import Context._

  val tests = Tests{
    test("read only field test") - {
      for {
        _ <- Main.setupUI()
        _ <- Context.services.clientSession.login("test", "test")
        _ <- waitCycle
        _ <- Future {
          Context.applicationInstance.goTo(EntityFormState(EntityKind.FORM.kind, Values.testFormName, "true", Some(JSONID(Vector(JSONKeyValue("id", "1"))).asString)))
        }
        _ <- waitCycle
        _ <- waitCycle
        _ <- Future {
          assert(document.getElementsByClassName(TestHooks.readOnlyField(Values.readOnlyField)).length == 1)
          assert(document.getElementsByClassName(TestHooks.readOnlyField(Values.readOnlyField)).item(0).isInstanceOf[HTMLElement])
          val field = document.getElementsByClassName(TestHooks.readOnlyField(Values.readOnlyField)).item(0).asInstanceOf[HTMLElement]
          assert(field.innerHTML == Values.readOnlyValue)
        }

      } yield true
    }
  }

}