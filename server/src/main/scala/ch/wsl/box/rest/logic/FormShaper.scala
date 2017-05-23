package ch.wsl.box.rest.logic

import io.circe._
import io.circe.syntax._
import ch.wsl.box.model.shared._
import ch.wsl.box.model.TablesRegistry
import ch.wsl.box.shared.utils.CSV
import io.circe.Json
import slick.lifted.Query
import slick.driver.PostgresDriver.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by andre on 5/18/2017.
  */

case class ReferenceKey(localField:String,remoteField:String,value:String)
case class Reference(association:Seq[ReferenceKey])

case class FormShaper(form:JSONForm)(implicit db:Database) extends UglyDBFilters {

  import ch.wsl.box.shared.utils.JsonUtils._


  private def createQuery(model:Json,subform: Subform):JSONQuery = {
    val filters = for{
      (local,remote) <- subform.localFields.split(",").zip(subform.subFields.split(","))
    } yield {
      JSONQueryFilter(remote,Some(Filter.EQUALS),model.hcursor.get[Json](local).right.get.toString())
    }
    JSONQuery(50,1,List(),filters.toList)
  }

  private def getSubform(model:Json, field:JSONField,form:JSONForm,subform:Subform):Future[Json] = {
    val query = createQuery(model,subform)

    for {
      form <- Forms(subform.id, form.lang)
      result <- FormShaper(form).extractArray(query)
    } yield result

  }



  private def toJson(json:Json):Future[Json] = {

    val values = form.fields.map{ field =>
      field.subform match {
        case None => Future.successful(field.key -> json.hcursor.get[Json](field.key).right.get)
        case Some(subform) => for{
          form <- Forms(subform.id,form.lang)
          result <- getSubform(json,field,form,subform)
        } yield field.key -> result
      }
    }
    Future.sequence(values).map(_.toMap.asJson)
  }

  private def extractSeq(query:JSONQuery):Future[Seq[Json]] = for{
    elements <- TablesRegistry.actions(form.table).getModel(query)
    result <- Future.sequence(elements.map(toJson))
  } yield result

  def extractArray(query:JSONQuery):Future[Json] = extractSeq(query).map(_.asJson)
  def extractOne(query:JSONQuery):Future[Json] = extractSeq(query).map(_.headOption.asJson)

  def csv(query:JSONQuery):Future[String] = for {
    results <- extractSeq(query)
  } yield {
    val strings = results.map { row =>
      form.tableFields.map { field =>
        row.get(field)
      }
    }
    CSV.of(strings)
  }

}
