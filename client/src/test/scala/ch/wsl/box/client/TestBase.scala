package ch.wsl.box.client

import org.scalajs.dom.{document, window}
import scribe.{Level, Logger, Logging}
import utest.{ArrowAssert, TestSuite}
import wvlet.airframe.Design

import scala.concurrent.{Future, Promise}

trait TestBase extends TestSuite with Logging {

  Logger.root.clearHandlers().clearModifiers().withHandler(minimumLevel = Some(Level.Debug)).replace()

  def injector:Design = TestModule.test

  Context.init(injector)

  def waitCycle:Future[Boolean] = {
    val promise = Promise[Boolean]
    window.setTimeout(() => {
      window.setTimeout(() => {
        promise.success(true)
      }, 2000)
    }, 2000)
    promise.future
  }

  def shouldBe(condition: Boolean) = {
    if(!condition) {

    }
    condition ==> true
  }

}
