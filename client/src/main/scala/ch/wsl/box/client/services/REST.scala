package ch.wsl.box.client.services

import ch.wsl.box.client.Context
import ch.wsl.box.client.routes.Routes
import ch.wsl.box.model.shared._
import io.circe.{Decoder, Json}
import org.scalajs.dom.File

import scala.concurrent.Future
import ch.wsl.box.shared.utils.JSONUtils._
import scribe.Logging
import kantan.csv._
import kantan.csv.ops._

/**
  * Created by andre on 4/24/2017.
  */
class REST(context:Context,httpClient:HttpClient) extends Logging {



  import io.circe.generic.auto._
  import ch.wsl.box.shared.utils.Formatters._
  import context._
  import ch.wsl.box.model.shared.EntityKind._

  private def client = httpClient.ForEndpoint(Routes.apiV1())

  def version() = client.get[String]("/version")
  def appVersion() = client.get[String]("/app_version")
  def validSession() = client.get[Boolean]("/validSession")
  def cacheReset() = httpClient.ForEndpoint("").get[String]("cache/reset")
  def serverReset() = httpClient.ForEndpoint("").get[String]("server/reset")

  def entities(kind:String):Future[Seq[String]] = client.get[Seq[String]](s"/${EntityKind(kind).plural}")

  //for entities and forms
  def specificKind(kind:String, lang:String, entity:String):Future[String] = client.get[String](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/kind")     //distinguish entities into table or view
  def list(kind:String, lang:String, entity:String, limit:Int): Future[Seq[Json]] = client.post[JSONQuery,Seq[Json]](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/list",JSONQuery.empty.limit(limit))
  def list(kind:String, lang:String, entity:String, query:JSONQuery): Future[Seq[Json]] = client.post[JSONQuery,Seq[Json]](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/list",query)
  def csv(kind:String, lang:String, entity:String, q:JSONQuery): Future[Seq[Seq[String]]] = client.post[JSONQuery,String](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/csv",q).map{ result =>
    result.asUnsafeCsvReader[Seq[String]](rfc).toSeq
  }
  def count(kind:String, lang:String, entity:String): Future[Int] = client.get[Int](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/count")
  def keys(kind:String, lang:String, entity:String): Future[Seq[String]] = client.get[Seq[String]](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/keys")
  def ids(kind:String, lang:String, entity:String, q:JSONQuery): Future[IDs] = client.post[JSONQuery,IDs](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/ids",q)
//  def schema(kind:String, lang:String, entity:String): Future[JSONSchema] = client.get[JSONSchema](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/schema")
  def metadata(kind:String, lang:String, entity:String): Future[JSONMetadata] = client.get[JSONMetadata](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/metadata")
  def tabularMetadata(kind:String, lang:String, entity:String): Future[JSONMetadata] = client.get[JSONMetadata](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/tabularMetadata")

  //only for forms
  def children(kind:String, entity:String, lang:String): Future[Seq[JSONMetadata]] = client.get[Seq[JSONMetadata]](s"/$kind/$lang/$entity/children")
  def lookup(lang:String,lookupEntity: String, map: JSONFieldMap, queryWithSubstitutions: Json): Future[Seq[JSONLookup]] = {
    queryWithSubstitutions.as[JSONQuery] match {
      case Right(query) => client.post[JSONQuery, Seq[JSONLookup]](s"/entity/$lang/$lookupEntity/lookup/${map.textProperty}/${map.valueProperty}", query)
      case Left(fail) => {
        Future.successful(Seq())
      }
    }
  }


  //for entities and forms
  def get(kind:String, lang:String, entity:String, id:JSONID):Future[Json] = client.get[Json](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/id/${id.asString}")
  def update(kind:String, lang:String, entity:String, id:JSONID, data:Json):Future[Int] = client.put[Json,Int](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/id/${id.asString}",data)
  def insert(kind:String, lang:String, entity:String, data:Json): Future[JSONID] = client.post[Json,JSONID](s"/${EntityKind(kind).entityOrForm}/$lang/$entity",data)
  def delete(kind:String, lang:String, entity:String, id:JSONID):Future[JSONCount] = client.delete[JSONCount](s"/${EntityKind(kind).entityOrForm}/$lang/$entity/id/${id.asString}")

  //files
  def sendFile(file:File, id:JSONID, entity:String): Future[Int] = client.sendFile[Int](s"/file/$entity/${id.asString}",file)

  //other utilsString
  def login(request:LoginRequest) = client.post[LoginRequest,Json]("/login",request)
  def logout() = client.get[String]("/logout")
  def labels(lang:String):Future[Map[String,String]] = client.get[Map[String,String]](s"/labels/$lang")
  def conf():Future[Map[String,String]] = client.get[Map[String,String]](s"/conf")
  def ui():Future[Map[String,String]] = client.get[Map[String,String]](s"/ui")
  def news(lang:String):Future[Seq[NewsEntry]] = client.get[Seq[NewsEntry]](s"/news/$lang")


  //export
  def dataMetadata(kind:String,name:String,lang:String) = client.get[JSONMetadata](s"/$kind/$name/metadata/$lang")
  def dataDef(kind:String,name:String,lang:String) = client.get[ExportDef](s"/$kind/$name/def/$lang")
  def dataList(kind:String,lang:String) = client.get[Seq[ExportDef]](s"/$kind/list/$lang")

  def data(kind:String,name:String,params:Json,lang:String):Future[Seq[Seq[String]]] = client.post[Json,String](s"/$kind/$name/$lang", params).map{ result =>
    result.asUnsafeCsvReader[Seq[String]](rfc).toSeq
  }

  def writeAccess(table:String,kind:String) = client.get[Boolean](s"/access/$kind/$table/write")

  //admin
  def generateStub(entity:String) = client.get[Boolean](s"/create-stub/$entity")

}
