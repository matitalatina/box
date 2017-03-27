package ch.wsl.box.client.components.navigation

import ch.wsl.box.client.controllers.{Controller, Container}
import ch.wsl.box.client.css.CommonStyles
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js
import scalacss.Defaults._
import scalacss.ScalaCssReact._


object TopNav {

  //Definisco lo stile del componente
  object Style extends StyleSheet.Inline {
    import dsl._
    val header = style(addClassNames("mdl-layout__header"))
    val row = style(addClassNames("mdl-layout__header-row"))

    val tabs:StyleA = style(addClassNames("mdl-layout__tab-bar","mdl-js-ripple-effect"))

    def tab(selected:Boolean) = selected match {
      case true => style(addClassNames("mdl-layout__tab","is-active"))
      case false => style(addClassNames("mdl-layout__tab"))
    }

  }


  //Definisco le proprieta' del componente
  case class Props(controller: Controller)


  //Definisco quando il componente deve essere aggiornato
  implicit val currentPageReuse = Reusability.by_==[Container]
  implicit val propsReuse = Reusability.by((_: Props).controller.container)


//  def menuClick(form:() => Container[Controller], ctrl:RouterCtl[Container[Controller]]) = for{
//    _ <- Callback.log("menuClick")
//    cb <- ctrl.set(form())
//  } yield cb

  //inizializzazione del componente
  val component = ScalaComponent.build[Props]("TopNav")
    .render_P { P =>

      println(P.controller.topMenu.map(_.name))

      <.header( Style.header,
        <.div( Style.row,
          <.div(CommonStyles.title, "PostgresRest UI")
        ),
        <.div(Style.tabs,
          P.controller.topMenu.toTagMod(item => <.a(Style.tab(item == P.controller.container), item.name, ^.onClick --> P.controller.topMenuClick(item)))
        )
      )
    }
    .build

  def apply(props: Props, ref: js.UndefOr[String] = "", key: js.Any = {}) = {
    println("Creating topnav")
    component(props)
  }

}


