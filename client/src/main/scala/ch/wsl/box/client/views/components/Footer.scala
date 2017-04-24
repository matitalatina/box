package ch.wsl.box.client.views.components
import ch.wsl.box.client.config.ExternalUrls
import ch.wsl.box.client.styles.{GlobalStyles}
import ch.wsl.box.client.styles.partials.FooterStyles
import org.scalajs.dom.raw.Element

import scalatags.JsDom.all._
import scalacss.ScalatagsCss._

object Footer {
  private lazy val template = footer(FooterStyles.footer)(
        p("WSL 2017")
  ).render

  def getTemplate: Element = template
}