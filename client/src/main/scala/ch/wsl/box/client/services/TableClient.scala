package ch.wsl.box.client.services

import ch.wsl.box.client.configs.Path
import ch.wsl.box.client.model.JSONResponse
import ch.wsl.box.model.shared._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON

/**
  * Created by andreaminetti on 23/02/16.
  */
class TableClient(model:String) {

  val paths = Path.forModel(model)

  def list(jsonQuery: JSONQuery) = AuthenticatedHttpClient.postJson[JSONResponse](paths.list,jsonQuery.asJson.noSpaces)
  def schema = AuthenticatedHttpClient.getString(paths.schema)
  def form = AuthenticatedHttpClient.get[Vector[JSONField]](paths.form)
  def keys = AuthenticatedHttpClient.get[Vector[String]](paths.keys)
  def count = AuthenticatedHttpClient.get[JSONCount](paths.count)
  def get(i:JSONKeys) = AuthenticatedHttpClient.getJs(paths.get(i.asString))
  def update(i:JSONKeys,data:js.Any) = AuthenticatedHttpClient.putJson[String](paths.update(i.asString),JSON.stringify(data))
  def insert(data:js.Any) = AuthenticatedHttpClient.postJson[String](paths.insert,JSON.stringify(data))
  def firsts = AuthenticatedHttpClient.get[JSONResponse](paths.firsts)

  object Helpers {

    def filter2table(filter: JSONQuery): Future[Table] = {

      def valueForKey(key:String, map: Map[String,Json]):String = {
        for{
          result <- map.lift(key).map { el =>
            (el.asNumber,el.asString) match {
              case (Some(n),_) => n.toString
              case (_,Some(s)) => s
              case (_,_) => el.toString
            }
          }
        } yield result
      }.getOrElse("")

      for {
        f <- form
        schema <- schema
        keys <- keys
        result <- list(filter)
      } yield {

        val jsonSchema = decode[JSONSchema](schema).getOrElse(JSONSchema.empty)

        val headers = f.map(_.key)
        val rows =
          result.data.map { row =>
            f.map { field =>
              field.key -> valueForKey(field.key,row)
            }
          }

        val jsonModel = JSONModel(jsonSchema,f.toSeq,keys.toSeq)

        Table(headers,rows,jsonModel)
      }
    }
  }

}

object TableClient{
  def apply(model:String) = new TableClient(model)

  def models() = AuthenticatedHttpClient.get[Vector[String]](Path.models)

}
