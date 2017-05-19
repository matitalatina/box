package ch.wsl.box.rest.logic

import io.circe._
import io.circe.syntax._
import ch.wsl.box.model.shared._
import ch.wsl.box.model.TablesRegistry
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

case class FormShaper[T <: slick.driver.PostgresDriver.api.Table[M],M <: Product](form:JSONForm)(implicit db:Database, encoder: Encoder[M]) extends UglyDBFilters {

  import ch.wsl.box.rest.logic.EnhancedTable._ //import col select


  private def createQuery(model:Json,subform: Subform):JSONQuery = {
    val filters = for{
      (local,remote) <- subform.localFields.split(",").zip(subform.subFields.split(","))
    } yield {
      JSONQueryFilter(remote,Some(Filter.EQUALS),model.hcursor.get[Json](local).right.get.toString())
    }
    JSONQuery(50,0,List(),filters.toList)
  }

  private def getSubform(model:Json, field:JSONField,form:JSONForm,subform:Subform):Future[Seq[Json]] = {
    val query = createQuery(model,subform)
    TablesRegistry.actions(form.table).getModel(query)
  }

  private def toJson(model:M):Future[Json] = {
    val json = model.asJson
    val values = form.fields.map{ field =>
      field.subform match {
        case None => Future.successful(field.key -> json.hcursor.get[Json](field.key).right.get)
        case Some(subform) => for{
          form <- Forms(subform.id,form.lang)
          result <- getSubform(json,field,form,subform)
        } yield field.key -> result.asJson
      }
    }
    Future.sequence(values).map(_.toMap.asJson)
  }

  private def extractSeq(query:Query[T,M,Seq]):Future[Seq[Json]] = for{
    elements <- db.run(query.result)
    result <- Future.sequence(elements.map(toJson))
  } yield result

  def extractArray(query:Query[T,M,Seq]):Future[Json] = extractSeq(query).map(_.asJson)
  def extractOne(query:Query[T,M,Seq]):Future[Json] = extractSeq(query).map(_.headOption.asJson)
}
