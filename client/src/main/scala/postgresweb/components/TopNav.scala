package postgresweb.components

import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.prefix_<^._
import postgresweb.css.CommonStyles
import postgresweb.routes.FormContainer

import scala.scalajs.js
import scalacss.Defaults._
import scalacss.ScalaCssReact._


object TopNav {

  //Definisco lo stile del componente
  object Style extends StyleSheet.Inline {
    import dsl._
    val header = style(addClassNames("mdl-layout__header"))
    val row = style(addClassNames("mdl-layout__header-row"))

    val tabs = style(addClassNames("mdl-layout__tab-bar","mdl-js-ripple-effect"))

    def tab(selected:Boolean) = selected match {
      case true => style(addClassNames("mdl-layout__tab","is-active"))
      case false => style(addClassNames("mdl-layout__tab"))
    }

  }


  //Definisco le proprieta' del componente
  case class Props(menus:Vector[() => FormContainer], selectedPage: FormContainer, ctrl: RouterCtl[FormContainer])


  //Definisco quando il componente deve essere aggiornato
  implicit val currentPageReuse = Reusability.by_==[FormContainer]
  implicit val propsReuse = Reusability.by((_: Props).selectedPage)


  def menuClick(form:() => FormContainer, ctrl:RouterCtl[FormContainer]) = for{
    _ <- Callback.log("menuClick")
    cb <- ctrl.set(form())
  } yield cb

  //inizializzazione del componente
  val component = ReactComponentB[Props]("TopNav")
    .render_P { P =>
      <.header( Style.header,
        <.div( Style.row,
          <.div(CommonStyles.title, "PostgresRest UI")
        ),
        <.div(Style.tabs,
          P.menus.map(item => <.a(Style.tab(item() == P.selectedPage), item().title, ^.onClick --> menuClick(item,P.ctrl) ))
        )
      )
    }
    .configure(Reusability.shouldComponentUpdate)
    .build

  def apply(props: Props, ref: js.UndefOr[String] = "", key: js.Any = {}) = component.set(key, ref)(props)

}


