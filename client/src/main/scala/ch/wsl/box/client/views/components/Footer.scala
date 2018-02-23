package ch.wsl.box.client.views.components
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.styles.partials.FooterStyles
import ch.wsl.box.client.utils.UI
import org.scalajs.dom.raw.Element

import scalatags.JsDom.all._
import scalacss.ScalatagsCss._

object Footer {
  private lazy val template = footer(FooterStyles.footer)(
        p(UI.footerCopyright, " - ", a(GlobalStyles.linkHeaderFooter,href := "https://github.com/Insubric/box","Box Framework"))
  ).render

  def getTemplate: Element = template
}