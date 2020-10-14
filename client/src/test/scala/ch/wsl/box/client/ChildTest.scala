package ch.wsl.box.client

import ch.wsl.box.client.mocks.Values
import ch.wsl.box.client.utils.TestHooks
import ch.wsl.box.model.shared.{FormActionsMetadata, JSONID, SharedLabels}
import org.scalajs.dom.document
import org.scalajs.dom.window
import org.scalajs.dom.ext._
import org.scalajs.dom.raw.HTMLElement
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
            document.getElementById(TestHooks.actionButton(SharedLabels.form.save)).asInstanceOf[HTMLElement].click()
          }
          _ <- waitCycle
          _ <- Future { //test if element is still present after save
            assert(document.getElementById(TestHooks.tableChildButtonId(2,Some(JSONID.fromMap(Map("id" -> "1"))))).isInstanceOf[HTMLElement])
            assert(countChilds(2) == 1)
            //navigate to another record
            val otherId = Some(JSONID.fromMap(Map("id" -> "2")).asString)
            Context.applicationInstance.goTo(EntityFormState("form", Values.testFormName, "true", otherId))
          }
          _ <- waitCycle
          _ <- Future {
            assert(countChilds(2) == 2)
            assert(document.getElementById(TestHooks.tableChildButtonId(2,Some(JSONID.fromMap(Map("id" -> "3"))))).isInstanceOf[HTMLElement])
          }
        } yield true

    }
  }

}