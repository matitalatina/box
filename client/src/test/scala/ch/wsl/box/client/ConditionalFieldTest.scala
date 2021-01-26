package ch.wsl.box.client

import ch.wsl.box.client.ChildTest.waitCycle
import ch.wsl.box.client.mocks.Values
import ch.wsl.box.client.utils.TestHooks
import ch.wsl.box.model.shared.{EntityKind, JSONID, JSONKeyValue}
import org.scalajs.dom.document
import org.scalajs.dom.raw.{Event, HTMLElement, HTMLInputElement}
import utest.{Tests, test}

import scala.concurrent.Future

object ConditionalFieldTest extends TestBase {

  import Context._

  val tests = Tests{
    test("conditional field test") - {

      def condidionalVisible() = document.getElementsByClassName(TestHooks.formField(values.conditionalField)).length > 0

      for {
        _ <- Main.setupUI()
        _ <- Context.services.clientSession.login("test", "test")
        _ <- waitCycle
        _ <- Future {
          Context.applicationInstance.goTo(EntityFormState(EntityKind.FORM.kind, values.testFormName, "true", Some(JSONID(Vector(JSONKeyValue("id", "1"))).asString), false))
        }
        _ <- waitCycle
        conditioner = document.querySelector(s".${TestHooks.formField(values.conditionerField)}").asInstanceOf[HTMLInputElement]
        _ <- Future {
          assert(!condidionalVisible())
          conditioner.value = values.conditionalValue
          conditioner.onchange(new Event("change"))
        }
        _ <- waitCycle
        _ <- Future {
          assert(condidionalVisible())
          conditioner.value = "something else"
          conditioner.onchange(new Event("change"))
        }
        _ <- waitCycle
        _ <- Future {
          assert(!condidionalVisible())
        }
      } yield true
    }
  }

}