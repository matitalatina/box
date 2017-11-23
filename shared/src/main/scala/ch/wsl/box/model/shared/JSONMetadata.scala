package ch.wsl.box.model.shared

import io.circe._
import io.circe.syntax._

/**
  * Created by andre on 5/16/2017.
  */
case class JSONMetadata(
                         id:Int,
                         name:String,
                         fields:Seq[JSONField],
                         layout:Layout,
                         table:String,
                         lang:String,
                         tableFields:Seq[String],
                         keys:Seq[String],
                         query:Option[JSONQuery]
                       )

object JSONMetadata{
  def jsonPlaceholder(form:JSONMetadata,subforms:Seq[JSONMetadata] = Seq()):Map[String,Json] = {
    form.fields.flatMap{ field =>
      val value:Option[Json] = (field.default,field.`type`) match {
        case (Some("arrayIndex"),_) => None
        case (Some("auto"),_) => None
        case (Some(d),JSONTypes.NUMBER) => Some(d.toDouble.asJson)
        case (Some(d),_) => Some(d.asJson)
        case (None,JSONTypes.NUMBER) => Some(0.asJson)
        case (None,JSONTypes.SUBFORM) => {
          for{
          subform <- field.subform
          sub <- subforms.find(_.id == subform.id)
          } yield jsonPlaceholder(sub,subforms).asJson
        }
        case (None,_) => Some("".asJson)
      }
      value.map{ v => field.key -> v }
    }.toMap
  }
}