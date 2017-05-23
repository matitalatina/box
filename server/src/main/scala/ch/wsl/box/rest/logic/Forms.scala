package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared._
import ch.wsl.box.rest.model.Field.{Field_i18n_row, Field_row}
import ch.wsl.box.rest.model.Form.{Form, Form_row}
import ch.wsl.box.rest.model.{Field, Form}
import ch.wsl.box.rest.service.Auth
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import io.circe._
import io.circe.parser._
import io.circe.syntax._

/**
  * Created by andreaminetti on 10/03/16.
  */
object Forms {
  def list: Future[Seq[String]] = Auth.boxDB.run{
    Form.table.result
  }.map{_.map(_.name)}


  private def fieldsToJsonFields(fields:Seq[(Field_row,Field_i18n_row)]) = {
    fields.map{ case (field,fieldI18n) =>
      val options = for{
        model <- field.refModel
        value <- field.refValueProperty
        text <- fieldI18n.refTextProperty
      } yield JSONFieldOptions(model,JSONFieldMap(value,text))
      JSONField(field.`type`,field.key,fieldI18n.title,options,fieldI18n.placeholder,field.widget)
    }
  }


  def apply(id:Int,lang:String):Future[JSONForm] = {
    val formQuery: Query[Form, Form_row, Seq] = for {
      form <- Form.table if form.id === id
    } yield form
    getForm(formQuery,lang)
  }
  
  def apply(name:String, lang:String = "en"):Future[JSONForm] = {
    val formQuery: Query[Form, Form_row, Seq] = for {
      form <- Form.table if form.name === name
    } yield form
    getForm(formQuery,lang)
  }

  def getForm(formQuery: Query[Form,Form_row,Seq],lang:String) = {

    import io.circe.generic.auto._

    def fieldQuery(formId:Int) = for{
      field <- Field.table if field.form_id === formId
      fieldI18n <- Field.Field_i18n if fieldI18n.lang === lang && fieldI18n.field_id === field.id
    } yield (field,fieldI18n)

    for{
      form <- Auth.boxDB.run( formQuery.result ).map(_.head)
      fields <- Auth.boxDB.run{fieldQuery(form.id.get).result}
      keys <- JSONSchemas.keysOf(form.table)
    } yield {



      val missingKeyFields = keys.filterNot(k => fields.map(_._1.key).contains(k)).map{ key =>
        JSONField("string",key)
      }

      val definedTableFields = form.tableFields.toSeq.flatMap(_.split(","))
      val missingKeyTableFields = keys.filterNot(k => definedTableFields.contains(k))
      val tableFields = missingKeyTableFields ++ definedTableFields

      val jsonFields = {missingKeyFields ++ fieldsToJsonFields(fields)}.distinct
      val layout = form.layout.map{l => parse(l).right.get.as[Layout].right.get}.getOrElse(Layout.fromFields(jsonFields))
      JSONForm(form.id.get,jsonFields,layout,form.table,lang,tableFields)
    }

  }
}
