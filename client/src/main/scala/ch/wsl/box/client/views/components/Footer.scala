package ch.wsl.box.client.views.components
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.utils.UI
import io.udash.bootstrap.BootstrapStyles
import scalatags.JsDom.all._
import scalacss.ScalatagsCss._

object Footer {
  def template(logo:Option[String]) = footer(
    div(BootstrapStyles.pullLeft)(p(UI.footerCopyright, " - ", a(href := "https://github.com/Insubric/box","Box Framework"))),
    div(BootstrapStyles.pullRight)( logo.map(x => img(GlobalStyles.headerLogo,src := x)))
  ).render
}