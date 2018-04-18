package ch.wsl.box.model.shared

import io.circe._
import io.circe.syntax._

/**
  * Created by andre on 5/16/2017.
  */
case class JSONMetadata(
                         objId:Int,
                         name:String,
                         label:String,
                         fields:Seq[JSONField],
                         layout:Layout,
                         entity:String,
                         lang:String,
                         tabularFields:Seq[String],
                         keys:Seq[String],
                         query:Option[JSONQuery],
                         exportView:Option[String]
                       )

object JSONMetadata{
  def jsonPlaceholder(form:JSONMetadata, subforms:Seq[JSONMetadata] = Seq()):Map[String,Json] = {
    form.fields.flatMap{ field =>
      val value:Option[Json] = (field.default,field.`type`) match {
        case (Some("arrayIndex"),_) => None
        case (Some("auto"),_) => None
        case (Some(d),JSONFieldTypes.NUMBER) => Some(d.toDouble.asJson)
        case (Some(d),_) => Some(d.asJson)
        case (None,JSONFieldTypes.NUMBER) => Some(0.asJson)
        case (None,JSONFieldTypes.CHILD) => {
          for{
            child <- field.child
            sub <- subforms.find(_.objId == child.objId)
          } yield jsonPlaceholder(sub,subforms).asJson
        }
        case (None,_) => Some("".asJson)
      }
      value.map{ v => field.name -> v }
    }.toMap
  }
}