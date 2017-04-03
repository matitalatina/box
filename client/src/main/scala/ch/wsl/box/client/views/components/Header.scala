package ch.wsl.box.client.views.components
import ch.wsl.box.client.IndexState
import ch.wsl.box.client.config.ExternalUrls
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.styles.partials.HeaderStyles
import org.scalajs.dom.raw.Element

import scalatags.JsDom.all._
import scalacss.ScalatagsCss._
import ch.wsl.box.client.Context._

object Header {
  private lazy val template = header(HeaderStyles.header)(
    div(GlobalStyles.body, GlobalStyles.clearfix)(
      div(HeaderStyles.headerLeft)(
        span("Box-Client")
      ),
      div(HeaderStyles.headerRight)(

      )
    )
  ).render

  def getTemplate: Element = template
}