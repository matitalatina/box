package ch.wsl.box.client.services

import ch.wsl.box.client.services.REST.get
import ch.wsl.box.model.shared._
import com.github.tototoshi.csv.{CSV, DefaultCSVFormat}
import io.circe.Json
import org.scalajs.dom.File

import scala.concurrent.Future

/**
  * Created by andre on 4/24/2017.
  */
object REST {


  import io.circe.generic.auto._
  import ch.wsl.box.shared.utils.Formatters._
  import ch.wsl.box.client.Context._
  import ch.wsl.box.model.shared.EntityKind._

  private def client = HttpClient("api/v1")


  def entities(kind:String):Future[Seq[String]] = client.get[Seq[String]](s"/${EntityKind(kind).plural}")

  //for entities and forms
  def specificKind(kind:String, lang:String, entity:String):Future[String] = client.get[String](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/kind")     //distinguish entities into table or view
  def list(kind:String, lang:String, entity:String, limit:Int): Future[Seq[Json]] = client.post[JSONQuery,Seq[Json]](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/list",JSONQuery.empty.limit(limit))
  def list(kind:String, lang:String, entity:String, query:JSONQuery): Future[Seq[Json]] = client.post[JSONQuery,Seq[Json]](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/list",query)
  def csv(kind:String, lang:String, entity:String, q:JSONQuery): Future[Seq[Seq[String]]] = client.post[JSONQuery,String](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/csv",q).map{ result =>
    CSV.read(result)
  }
  def count(kind:String, lang:String, entity:String): Future[Int] = client.get[Int](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/count")
  def keys(kind:String, lang:String, entity:String): Future[Seq[String]] = client.get[Seq[String]](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/keys")
  def ids(kind:String, lang:String, entity:String, q:JSONQuery): Future[IDs] = client.post[JSONQuery,IDs](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/ids",q)
//  def schema(kind:String, lang:String, entity:String): Future[JSONSchema] = client.get[JSONSchema](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/schema")
  def metadata(kind:String, lang:String, entity:String): Future[JSONMetadata] = client.get[JSONMetadata](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/metadata")

  //only for forms
  def children(entity:String, lang:String): Future[Seq[JSONMetadata]] = client.get[Seq[JSONMetadata]](s"/form/$lang/$entity/children")

  //only for entities
  def get(kind:String, lang:String, entity:String, id:JSONID):Future[Json] = client.get[Json](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/id/${id.asString}")
  def update(kind:String, lang:String, entity:String, id:JSONID, data:Json):Future[Json] = client.put[Json,Json](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/id/${id.asString}",data)
  def insert(kind:String, lang:String, entity:String, data:Json): Future[Json] = client.post[Json,Json](s"/${EntityKind(kind).entityOrForm}/$lang/$entity",data)
  def delete(kind:String, lang:String, entity:String, id:JSONID):Future[JSONCount] = client.delete[JSONCount](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/id/${id.asString}")

  //files
  def sendFile(file:File, id:JSONID, entity:String): Future[Int] = client.sendFile[Int](s"/file/$entity/${id.asString}",file)

  //other utilsString
  def login(request:LoginRequest) = client.post[LoginRequest,Json]("/login",request)
  def logout() = client.get[String]("/logout")
  def labels(lang:String):Future[Map[String,String]] = client.get[Map[String,String]](s"/labels/$lang")
  def conf():Future[Map[String,String]] = client.get[Map[String,String]](s"/conf")
  def ui():Future[Map[String,String]] = client.get[Map[String,String]](s"/ui")


  //export
  def exportMetadata(name:String,lang:String) = client.get[JSONMetadata](s"/export/$name/metadata/$lang")
  def export(name:String,params:Seq[Json],lang:String):Future[Seq[Seq[String]]] = client.post[Seq[Json],String](s"/export/$name/$lang",params).map(CSV.read)
  def exports(lang:String) = client.get[Seq[ExportDef]](s"/export/list/$lang")

  def writeAccess(table:String) = client.get[Boolean](s"/access/table/$table/write")

}
