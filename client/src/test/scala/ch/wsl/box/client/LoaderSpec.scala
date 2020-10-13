package ch.wsl.box.client

import org.scalajs.dom.document
import org.scalajs.dom.ext._
import org.scalatest.flatspec.AnyFlatSpec


class LoaderSpec extends AnyFlatSpec  {

  val clientConf:Map[String,String] = Map()
  val uiConf:Map[String,String] = Map(
    "title" -> "Test Title"
  )
  val labels:Map[String,String] = Map()
  val appVersion:String = "TEST"
  val version:String = "TEST"

  Context.init(Module.prod)

  Main.setupUI(
    clientConf,
    labels,
    uiConf,
    version,
    appVersion
  )

  "Index page" should "have the title defined in conf" in {
    assert(document.querySelectorAll("#headerTitle").count(_.textContent == uiConf("title")) == 1)
  }

}
