package ch.wsl.box.rest.logic

import ch.wsl.box.model.shared.{JSONField, JSONFieldMap, JSONFieldOptions}
import ch.wsl.box.rest.model.{Field, Form}
import ch.wsl.box.rest.service.Auth
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by andreaminetti on 10/03/16.
  */
object Forms {
  def list: Future[Seq[String]] = Auth.boxDB.run{
    Form.table.result
  }.map{_.map(_.name)}

  def apply(name:String, lang:String = "en"):Future[Seq[JSONField]] = {
    val q = for{
      form <- Form.table if form.name == name
      fields <- Field.table zip Field.Field_i18n if fields._1.form_id === form.id && fields._2.lang === lang
    } yield fields

    Auth.boxDB.run{
      q.result
    } map { _.map{ case (field,fieldI18n) =>
      val options = for{
        model <- field.refModel
        value <- field.refValueProperty
        text <- fieldI18n.refTextProperty
      } yield JSONFieldOptions(model,JSONFieldMap(value,text))
      JSONField(field.`type`,field.table,field.key,fieldI18n.title,options,fieldI18n.placeholder,field.widget)
    }}
  }
}
