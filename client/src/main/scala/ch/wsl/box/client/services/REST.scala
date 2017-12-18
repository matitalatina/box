package ch.wsl.box.client.services

import ch.wsl.box.client.services.REST.get
import ch.wsl.box.model.shared._
import io.circe.Json
import org.scalajs.dom.File

import scala.concurrent.Future

/**
  * Created by andre on 4/24/2017.
  */
object REST {


  import io.circe.generic.auto._
  import ch.wsl.box.shared.utils.Formatters._
  import scalajs.concurrent.JSExecutionContext.Implicits.queue


  private def client = HttpClient("/api/v1")

  private def entityOrForm(kind:String) = kind match{
    case "table"|"view" => "entity"
    case _ => kind
  }

  def sendFile(file:File, ids:JSONIDs, entity:String) = client.sendFile[Int](s"/file/$entity/${ids.asString}",file)

  def entities(kind:String):Future[Seq[String]] = kind match {
      case "entity" => client.get[Seq[String]](s"/entities")
      case _ => client.get[Seq[String]](s"/${kind}s")   //for tables, views and forms
  }

  //for entities and forms
  def specificKind(kind:String, lang:String, entity:String):Future[String] = client.get[String](s"/${entityOrForm(kind)}/$lang/$entity/kind")
  def list(kind:String, lang:String, entity:String, limit:Int): Future[Seq[Json]] = client.post[JSONQuery,JSONData[Json]](s"/${entityOrForm(kind)}/$lang/$entity/list",JSONQuery.empty.limit(limit)).map(_.data)
  def list(kind:String, lang:String, entity:String, query:JSONQuery): Future[Seq[Json]] = client.post[JSONQuery,JSONData[Json]](s"/${entityOrForm(kind)}/$lang/$entity/list",query).map(_.data)
  def csv(kind:String, lang:String, entity:String, q:JSONQuery): Future[Seq[Seq[String]]] = client.post[JSONQuery,String](s"/${entityOrForm(kind)}/$lang/$entity/csv",q).map{ result =>
    result.split("\r\n").toSeq.map{x => x.substring(1,x.length-1).split("\",\"").toSeq}
  }
  def count(kind:String, lang:String, entity:String): Future[Int] = client.get[Int](s"/${entityOrForm(kind)}/$lang/$entity/count")
  def keys(kind:String, lang:String, entity:String): Future[Seq[String]] = client.get[Seq[String]](s"/${entityOrForm(kind)}/$lang/$entity/keys")
  def ids(kind:String, lang:String, entity:String, q:JSONQuery): Future[IDs] = client.post[JSONQuery,IDs](s"/${entityOrForm(kind)}/$lang/$entity/ids",q)
//  def schema(kind:String, lang:String, entity:String): Future[JSONSchema] = client.get[JSONSchema](s"/${entityOrForm(kind)}/$lang/$entity/schema")
  def metadata(kind:String, lang:String, entity:String): Future[JSONMetadata] = client.get[JSONMetadata](s"/${entityOrForm(kind)}/$lang/$entity/metadata")

  //only for forms
  def children(entity:String, lang:String): Future[Seq[JSONMetadata]] = client.get[Seq[JSONMetadata]](s"/form/$lang/$entity/children")

  //only for entities
  def get(kind:String, lang:String, entity:String, ids:JSONIDs):Future[Json] = client.get[Json](s"/${entityOrForm(kind)}/$lang/$entity/id/${ids.asString}")
  def update(kind:String, lang:String, entity:String, ids:JSONIDs, data:Json):Future[Json] = client.put[Json,Json](s"/${entityOrForm(kind)}/$lang/$entity/id/${ids.asString}",data)
  def insert(kind:String, lang:String, entity:String, data:Json): Future[Json] = client.post[Json,Json](s"/${entityOrForm(kind)}/$lang/$entity",data)
  def delete(kind:String, lang:String, entity:String, ids:JSONIDs):Future[JSONCount] = client.delete[JSONCount](s"/${entityOrForm(kind)}/$lang/$entity/id/${ids.asString}")

  //other utils
  def loginCheck() = client.get[String]("/checkLogin")
  def labels(lang:String):Future[Map[String,String]] = client.get[Map[String,String]](s"/labels/$lang")
  def conf():Future[Map[String,String]] = client.get[Map[String,String]](s"/conf")
}
