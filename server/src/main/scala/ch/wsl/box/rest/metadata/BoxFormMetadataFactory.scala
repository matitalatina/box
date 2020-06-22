package ch.wsl.box.rest.metadata

import akka.stream.Materializer
import ch.wsl.box.model.boxentities.BoxForm
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.routes.Table
import ch.wsl.box.rest.utils.UserProfile
import scribe.Logging

import scala.concurrent.{ExecutionContext, Future}

case class BoxFormMetadataFactory(implicit up:UserProfile, mat:Materializer, ec:ExecutionContext) extends Logging with MetadataFactory {



  import ch.wsl.box.jdbc.PostgresProfile.api._


  import ch.wsl.box.rest.metadata.box.Constants._
  import ch.wsl.box.rest.metadata.box._





  def registry = for{
    forms <- getForms()
  } yield Seq(
    FormUIDef.main(Table.tables.toSeq.sorted),
    FormUIDef.field(forms),
    FormUIDef.fieldI18n,
    FormUIDef.formI18n,
    FormUIDef.fieldFile,
    FunctionUIDef.main,
    FunctionUIDef.field,
    FunctionUIDef.fieldI18n,
    FunctionUIDef.functionI18n,
  )

  def getForms():Future[Seq[BoxForm.BoxForm_row]] = {
    up.boxDb.run{
      BoxForm.BoxFormTable.result
    }
  }

  val visibleAdmin = Seq(FUNCTION,FORM)

  override def list: Future[Seq[String]] = registry.map(_.filter(f => visibleAdmin.contains(f.objId)).map(_.name))

  override def of(name: String, lang: String): Future[JSONMetadata] = registry.map(_.find(_.name == name).get)

  override def of(id: Int, lang: String): Future[JSONMetadata] = registry.map(_.find(_.objId == id).get)

  override def children(form: JSONMetadata): Future[Seq[JSONMetadata]] = getForms().map{ forms =>
    form match {
      case f if f.objId == FORM => Seq(FormUIDef.field(forms),FormUIDef.fieldI18n,FormUIDef.formI18n,FormUIDef.fieldFile)
      case f if f.objId == FORM_FIELD => Seq(FormUIDef.fieldI18n,FormUIDef.fieldFile)
      case f if f.objId == FUNCTION => Seq(FunctionUIDef.field,FunctionUIDef.fieldI18n,FunctionUIDef.functionI18n)
      case f if f.objId == FUNCTION_FIELD => Seq(FunctionUIDef.fieldI18n)
      case _ => Seq()
    }
  }

}
