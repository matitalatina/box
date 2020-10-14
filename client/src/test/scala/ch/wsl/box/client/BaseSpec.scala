package ch.wsl.box.client

import org.scalajs.dom
import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import wvlet.airframe.Design

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.util.Try

class BaseSpec extends AsyncFlatSpec with Matchers {


  def injector:Design = TestModule.test


  def load():Future[Unit] = {
    Context.init(injector, executionContext)
    Main.setupUI()
  }

}
