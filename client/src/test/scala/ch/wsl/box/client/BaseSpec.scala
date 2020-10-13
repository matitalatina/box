package ch.wsl.box.client

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import wvlet.airframe.Design

import scala.concurrent.Future

class BaseSpec extends AsyncFlatSpec with Matchers {


  def injector:Design = TestModule.test


  def load():Future[Unit] = {
    Context.init(injector, executionContext)
    Main.setupUI()
  }

}
