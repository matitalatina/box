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
  *
  * translate db data to JSONForm structure
  */

case class ReferenceKey(localField:String,remoteField:String,value:String)
case class Reference(association:Seq[ReferenceKey])

case class FormShaper(form:JSONMetadata)(implicit db:Database) extends UglyDBFilters {

  import ch.wsl.box.shared.utils.JsonUtils._

  def jsonFormMetadata = JSONFormMetadata()

  private def createQuery(model:Json,subform: Subform):JSONQuery = {
    val parentFilter = for{
      (local,remote) <- subform.localFields.split(",").zip(subform.subFields.split(","))
    } yield {
      JSONQueryFilter(remote,Some(Filter.EQUALS),model.get(local))
    }

    val filters = parentFilter.toSeq ++ subform.subFilter

    JSONQuery(None,List(),filters.toList.distinct)
  }

  private def getSubform(model:Json, field:JSONField, form:JSONMetadata, subform:Subform):Future[Json] = {
    val query = createQuery(model,subform)

    for {
      result <- FormShaper(form).extractArray(query)
    } yield result

  }



  private def expandJson(json:Json):Future[Json] = {

    val values = form.fields.map{ field =>
      {(field.`type`,field.subform) match {
        case ("static",_) => Future.successful(field.key -> field.default.asJson)  //set default value
        case (_,None) => Future.successful(field.key -> json.js(field.key))        //use given value
        case (_,Some(subform)) => for{
          form <- jsonFormMetadata.get(subform.id,form.lang)
          result <- getSubform(json,field,form,subform)
        } yield field.key -> result
      }}.recover{ case t =>
        t.printStackTrace()
        field.key -> Json.Null
      }
    }
    Future.sequence(values).map(_.toMap.asJson)
  }

  private def extractSeq(query:JSONQuery):Future[Seq[Json]] = {
    for{
      elements <- TablesRegistry.actions(form.table).getModel(query)
      result <- Future.sequence(elements.map(expandJson))
    } yield result
  }.recover{ case t =>
    t.printStackTrace()
    Seq()
  }

  def extractArray(query:JSONQuery):Future[Json] = extractSeq(query).map(_.asJson)     //todo adapt JSONQuery to select only fields in form
  def extractOne(query:JSONQuery):Future[Json] = extractSeq(query).map(x => if(x.length >1) throw new Exception("Multiple rows retrieved with single key") else x.headOption.asJson)

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

  def updateAll(e:Json):Future[Int] = {

    def subforms = form.fields.filter(_.subform.isDefined).map { field =>
      for {
        form <- jsonFormMetadata.get(field.subform.get.id, form.lang)
        subJson = e.seq(field.key)
        result <- Future.sequence{
          subJson.map{ json =>
            FormShaper(form).updateAll(json).recover{case t => t.printStackTrace(); 1}
        }}
      } yield result
    }
    val key = e.keys(form.keys)
    val table = TablesRegistry.actions(form.table)
    for{
      _ <- Future.sequence(subforms)
      existing <- table.getById(key).recover{ case t => println("recovered future with none"); None }   //existing record in db
      result <- {
        if(existing.isDefined) {
          println(s"update $key")
          table.update(key,e)
        } else {
          println(s"insert $e")
          table.insert(e).map(_ => 1)
        }
      }
    } yield result

  }

  def insertAll(e:Json):Future[Json] = {
    form.fields.filter(_.subform.isDefined).foreach { field =>
      for {
        form <- jsonFormMetadata.get(field.subform.get.id, form.lang)
        rows = e.seq(field.key)
        result <- Future.sequence(rows.map(row => FormShaper(form).insertAll(row)))
      } yield result
    }
    TablesRegistry.actions(form.table).insert(e)
  }

}
