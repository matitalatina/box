package ch.wsl.box.client.mocks

import ch.wsl.box.client.services.REST
import ch.wsl.box.model.shared.{Child, ExportDef, FormActionsMetadata, IDs, JSONCount, JSONField, JSONFieldMap, JSONFieldTypes, JSONID, JSONKeyValue, JSONLookup, JSONMetadata, JSONQuery, Layout, LayoutBlock, LoginRequest, NewsEntry, SharedLabels, WidgetsNames}
import io.circe.Json
import io.circe.syntax._
import org.scalajs.dom.File

import scala.concurrent.Future

class RestMock extends REST {
  override def version(): Future[String] = Future.successful("version")

  override def appVersion(): Future[String] = Future.successful("appVersion")

  override def validSession(): Future[Boolean] = Future.successful{
    true
  }

  override def cacheReset(): Future[String] = {
    println("cacheReset not implemented")
    ???
  }

  override def serverReset(): Future[String] = {
    println("serverReset not implemented")
    ???
  }

  override def entities(kind: String): Future[Seq[String]] = {
    kind match {
      case "form" => Future.successful(Values.formEntities)
      case _ => {
        println(s"entities for $kind not implemented")
        ???
      }
    }
  }

  override def specificKind(kind: String, lang: String, entity: String): Future[String] = {
    println("specificKind not implemented")
    ???
  }

  override def list(kind: String, lang: String, entity: String, limit: Int): Future[Seq[Json]] = {
    println("list1 not implemented")
    ???
  }

  override def list(kind: String, lang: String, entity: String, query: JSONQuery): Future[Seq[Json]] = {
    println("list2 not implemented")
    ???
  }

  override def csv(kind: String, lang: String, entity: String, q: JSONQuery): Future[Seq[Seq[String]]] = {
    println("csv not implemented")
    ???
  }

  override def count(kind: String, lang: String, entity: String): Future[Int] = {
    println("count not implemented")
    ???
  }

  override def keys(kind: String, lang: String, entity: String): Future[Seq[String]] = {
    println("keys not implemented")
    ???
  }

  override def ids(kind: String, lang: String, entity: String, q: JSONQuery): Future[IDs] = {
    println("ids not implemented")
    ???
  }

  override def metadata(kind: String, lang: String, entity: String): Future[JSONMetadata] = Future.successful{
    Values.metadata
  }

  override def tabularMetadata(kind: String, lang: String, entity: String): Future[JSONMetadata] = {
    println("tabularMetadata not implemented")
    ???
  }

  override def children(kind: String, entity: String, lang: String): Future[Seq[JSONMetadata]] = Future.successful{
    Seq(Values.childMetadata)
  }

  override def lookup(lang: String, lookupEntity: String, map: JSONFieldMap, queryWithSubstitutions: Json): Future[Seq[JSONLookup]] = {
    println("lookup not implemented")
    ???
  }

  override def get(kind: String, lang: String, entity: String, id: JSONID): Future[Json] = Future.successful{
    id.values.head match {
      case "1" => Map(
        "id" -> 1.asJson,
        "child" -> Seq(
          Map("parent_id" -> 1, "id" -> 1)
        ).asJson
      ).asJson
      case "2" => Map(
        "id" -> 2.asJson,
        "child" -> Seq(
          Map("parent_id" -> 2, "id" -> 2),
          Map("parent_id" -> 2, "id" -> 3)
        ).asJson
      ).asJson
    }
  }

  override def update(kind: String, lang: String, entity: String, id: JSONID, data: Json): Future[Int] = {
    println("update not implemented")
    ???
  }

  override def insert(kind: String, lang: String, entity: String, data: Json): Future[JSONID] = Future.successful{
    JSONID(id = Vector(JSONKeyValue("id","1")))
  }

  override def delete(kind: String, lang: String, entity: String, id: JSONID): Future[JSONCount] = {
    println("delete not implemented")
    ???
  }

  override def sendFile(file: File, id: JSONID, entity: String): Future[Int] = {
    println("sendFile not implemented")
    ???
  }

  override def login(request: LoginRequest): Future[Json] = Future.successful{
    Json.True
  }

  override def logout(): Future[String] = {
    println("logout not implemented")
    ???
  }

  override def labels(lang: String): Future[Map[String, String]] = {
    Future.successful(lang match {
      case "en" => Map(
        SharedLabels.header.lang -> Values.headerLangEn
      )
      case "it" => Map(
        SharedLabels.header.lang -> Values.headerLangIt
      )
    })
  }

  override def conf(): Future[Map[String, String]] = Future.successful{
    Values.conf
  }

  override def ui(): Future[Map[String, String]] = Future.successful{
    Values.uiConf
  }

  override def news(lang: String): Future[Seq[NewsEntry]] = {
    println("news not implemented")
    ???
  }

  override def dataMetadata(kind: String, name: String, lang: String): Future[JSONMetadata] = {
    println("dataMetadata not implemented")
    ???
  }

  override def dataDef(kind: String, name: String, lang: String): Future[ExportDef] = {
    println("dataDef not implemented")
    ???
  }

  override def dataList(kind: String, lang: String): Future[Seq[ExportDef]] = {
    println("dataList not implemented")
    ???
  }

  override def data(kind: String, name: String, params: Json, lang: String): Future[Seq[Seq[String]]] = {
    println("data not implemented")
    ???
  }

  override def writeAccess(table: String, kind: String): Future[Boolean] = {
    println("writeAccess not implemented")
    ???
  }

  override def generateStub(entity: String): Future[Boolean] = {
    println("generateStub not implemented")
    ???
  }
}
