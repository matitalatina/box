package ch.wsl.box.client

import ch.wsl.box.client.services.{ClientConf, Labels, REST, UI}
import ch.wsl.box.client.styles.OpenLayersStyles
import ch.wsl.box.client.utils._
import io.udash.wrappers.jquery._
import org.scalajs.dom
import org.scalajs.dom.{Element, document}
import scribe.{Level, Logger, Logging}

object Main extends Logging {
  import Context._

  def main(args: Array[String]): Unit = {

    Context.init(Module.prod)

    println(
      s"""
         |===================================
         |
         |    _/_/_/      _/_/    _/      _/
         |   _/    _/  _/    _/    _/  _/
         |  _/_/_/    _/    _/      _/
         | _/    _/  _/    _/    _/  _/
         |_/_/_/      _/_/    _/      _/
         |
         |===================================
         |
         |Box client started
         |
         |""".stripMargin)



    Logger.root.clearHandlers().clearModifiers().withHandler(minimumLevel = Some(Level.Debug)).replace()

    for {
      appVersion <- services.rest.appVersion()
      version <- services.rest.version()
      clientConf <- services.rest.conf()
      uiConf <- services.rest.ui()
      labels <- services.rest.labels(services.clientSession.lang())
    } yield {

      document.addEventListener("DOMContentLoaded", { (e: dom.Event) =>
        setupUI(clientConf,labels,uiConf,version,appVersion)
      })
    }
  }

  def setupUI(
             clientConf: Map[String,String],
             labels: Map[String,String],
             uiConf:Map[String,String],
             version:String,
             appVersion:String
             ): Unit = {


    //loads datetime picker
    typings.bootstrap.bootstrapRequire

    //require font
    styles.fonts.ClearSans

    ClientConf.load(clientConf,version,appVersion)
    Labels.load(labels)
    UI.load(uiConf)

    val title = document.getElementsByTagName("title").item(0)
    if(title != null) title.innerHTML = UI.title.getOrElse(s"Box")

    val CssSettings = scalacss.devOrProdDefaults
    import CssSettings._

    val mainStyle = document.createElement("style")
    mainStyle.innerText = ClientConf.style.render(cssStringRenderer,cssEnv)

    val olStyle = document.createElement("style")
    olStyle.innerText = OpenLayersStyles.render(cssStringRenderer,cssEnv)

    document.body.appendChild(mainStyle)
    document.body.appendChild(olStyle)

    val app = document.createElement("div")
    document.body.appendChild(app)
    applicationInstance.run(app)



  }
}
