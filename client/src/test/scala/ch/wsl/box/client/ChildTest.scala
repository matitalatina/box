package ch.wsl.box.client

import ch.wsl.box.client.mocks.Values
import ch.wsl.box.client.utils.TestHooks
import ch.wsl.box.model.shared.{FormActionsMetadata, JSONID, SharedLabels}
import org.scalajs.dom.{KeyboardEventInit, document, window}
import org.scalajs.dom.ext._
import org.scalajs.dom.raw.{Event, HTMLElement, HTMLInputElement, KeyboardEvent}
import utest._

import scala.concurrent.Future

object ChildTest extends TestSuite with TestBase {

  import Context._

  def countChilds(id:Int) = document.querySelectorAll(s"#${TestHooks.tableChildId(id)} .${TestHooks.tableChildRow}").length

  val tests = Tests{
    test("child test") - {

        for {
          _ <- Main.setupUI()
          _ <- Context.services.clientSession.login("test", "test")
          _ <- waitCycle
          _ <- Future {
            Context.applicationInstance.goTo(EntityFormState("form", Values.testFormName, "true", None))
          }
          _ <- waitCycle
          _ <- Future {
            assert(document.querySelectorAll("h3 span").count(_.textContent.contains(Values.testFormTitle)) == 1)
            assert(document.getElementById(TestHooks.tableChildId(2)).isInstanceOf[HTMLElement])
            assert(countChilds(2) == 0)
            document.getElementById(TestHooks.addChildId(2)).asInstanceOf[HTMLElement].click()
          }
          _ <- waitCycle
          _ <- Future {
            assert(countChilds(2) == 1)
            val input = document.querySelector(s".${TestHooks.formField("text")}").asInstanceOf[HTMLInputElement]
            input.value = "test"
            input.onchange(new Event("change"))
          }
          _ <- waitCycle
          _ <- Future {
            assert(countChilds(2) == 1)
            assert(document.getElementById(TestHooks.dataChanged) != null)
            document.getElementById(TestHooks.actionButton(SharedLabels.form.save)).asInstanceOf[HTMLElement].click()
          }
          _ <- waitCycle
          _ <- Future { //test if element is still present after save
            assert(document.getElementById(TestHooks.tableChildButtonId(2,Some(Values.ids.main.singleChild))).isInstanceOf[HTMLElement])
            assert(countChilds(2) == 1)
            //navigate to another record
            val otherId = Some(JSONID.fromMap(Map("id" -> "2")).asString)
            Context.applicationInstance.goTo(EntityFormState("form", Values.testFormName, "true", Some(Values.ids.main.doubleChild.asString)))
          }
          _ <- waitCycle
          _ <- Future {
            assert(countChilds(2) == 2)
            assert(document.getElementById(TestHooks.tableChildButtonId(2,Some(Values.ids.childs.thirdChild))).isInstanceOf[HTMLElement])
          }

        } yield true

    }
  }

}