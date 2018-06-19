package ch.wsl.box.model.shared

import io.circe._
import io.circe.syntax._
import ch.wsl.box.shared.utils.JsonUtils._
import scribe.Logging

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
                         exportView:Option[String],
                         baseTable:String
                       )

object JSONMetadata extends Logging {
  def jsonPlaceholder(form:JSONMetadata, subforms:Seq[JSONMetadata] = Seq()):Map[String,Json] = {
    form.fields.flatMap{ field =>
      val value:Option[Json] = (field.default,field.`type`) match {
        case (Some("arrayIndex"),_) => None
        case (Some("auto"),_) => None
        case (Some(d),JSONFieldTypes.NUMBER) => Some(d.toDouble.asJson)
        case (Some(d),_) => Some(d.asJson)
        case (None,JSONFieldTypes.NUMBER) => None
        case (None,JSONFieldTypes.CHILD) => {
          for{
            child <- field.child
            sub <- subforms.find(_.objId == child.objId)
          } yield jsonPlaceholder(sub,subforms).asJson
        }
        case (None,_) => None
      }
      value.map{ v => field.name -> v }
    }.toMap
  }

  def extractFields(fields:Seq[Either[String,SubLayoutBlock]]):Seq[String] = fields.flatMap{
    case Left(s) => Seq(s)
    case Right(sub) => extractFields(sub.fields)
  }

  def hasData(json:Json,keys:Seq[String]):Boolean = {
    logger.info(s"looking for data in $json with keys $keys")
    !keys.forall(key => json.getOpt(key).isEmpty && !hasData(json.js(key),keys))
  }
}