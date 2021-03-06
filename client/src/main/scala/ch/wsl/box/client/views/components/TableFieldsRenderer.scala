package ch.wsl.box.client.views.components

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.client.services.ClientConf
import ch.wsl.box.client.styles.GlobalStyles
import ch.wsl.box.client.{EntityFormState, EntityTableState}
import ch.wsl.box.model.shared.{JSONField, JSONFieldTypes, JSONID, WidgetsNames}
import io.circe.Json
import org.scalajs.dom
import scalacss.ScalatagsCss._
import org.scalajs.dom.{Element, Event}
import io.udash._
import scribe.Logging
import scalatags.JsDom.TypedTag

/**
  * Created by andre on 5/2/2017.
  */
object TableFieldsRenderer extends Logging{

  import io.circe.syntax._

  import scalatags.JsDom.all._



  def toggleEdit(editing:Property[Boolean]) = {
    logger.info("Toggle edit")
    editing.set(!editing.get)
  }

  def apply(value:String, field:JSONField, keys:JSONID, routes:Routes):TypedTag[Element] = {


    val contentFixed = (field.lookup,field.widget) match {
      case (Some(opts),_) => {
        val label: String = opts.lookup.find(_.id == value).map(_.value).getOrElse(value)
        val finalLabel = if(label.trim.length > 0) label else value
        p(finalLabel)
//        a(href := routes.edit(JSONKeys.fromMap(Map(field.key -> value)).asString).url,finalLabel)
      }
      case (None,Some(WidgetsNames.richTextEditor)) => p(raw(value))
      case (None,_) => p(ClientConf.style.preformatted,value)
    }

//    val editing = Property(false)
//    val model = Property(value.asJson)
//
//    div(onclick :+= ((ev: Event) => toggleEdit(editing), true),
//      showIf(editing.transform(x => !x))(contentFixed.render),
//      showIf(editing)(div(JSONSchemaRenderer.fieldRenderer(field,model,keys.keys.map(_.key),false)).render)
//    )

    def align = field.`type` match{
      case JSONFieldTypes.NUMBER => if (field.lookup.isEmpty) ClientConf.style.numberCells else ClientConf.style.lookupCells
      case JSONFieldTypes.DATE | JSONFieldTypes.DATETIME | JSONFieldTypes.TIME => ClientConf.style.dateCells
      case _ => ClientConf.style.textCells
    }

    div(align)(contentFixed)


  }
}
