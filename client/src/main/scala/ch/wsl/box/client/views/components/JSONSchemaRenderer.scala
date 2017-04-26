package ch.wsl.box.client.views.components

import ch.wsl.box.model.shared.JSONSchema


/**
  * Created by andre on 4/25/2017.
  */
object JSONSchemaRenderer {

  import ch.wsl.box.client.Context._
  import scalatags.JsDom.all._

  def apply(schema:JSONSchema) = {
    div(
      h1("")
    )
  }
}
