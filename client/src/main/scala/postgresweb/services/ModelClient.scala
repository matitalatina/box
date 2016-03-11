package postgresweb.services

import io.circe._
import io.circe.syntax._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._

import postgresweb.configs.Path
import postgresweb.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

/**
  * Created by andreaminetti on 23/02/16.
  */
class ModelClient(model:String) {

  val paths = Path.forModel(model)

  def list(jsonQuery: JSONQuery) = HttpClient.postJson[JSONResponse](paths.list,jsonQuery.asJson.noSpaces)
  def schema = HttpClient.getString(paths.schema)
  def form = HttpClient.get[Vector[JSONField]](paths.form)
  def keys = HttpClient.get[Vector[String]](paths.keys)
  def count = HttpClient.get[JSONCount](paths.count)
  def get(i:String) = HttpClient.getJs(paths.get(i))
  def update(i:String,data:js.Any) = HttpClient.putJson[String](paths.update(i),data.toString)
  def insert(data:js.Any) = HttpClient.postJson[String](paths.insert,data.toString)
  def firsts = HttpClient.get[JSONResponse](paths.firsts)

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
        result <- list(filter)
      } yield {

        val jsonSchema = decode[JSONSchema](schema).getOrElse(JSONSchema.empty)

        val headers = f.map(_.key)
        val rows =
          result.data.map { row =>
            f.map { field =>
              valueForKey(field.key,row)
            }
          }
        Table(headers,rows,jsonSchema)
      }
    }
  }

}

object   ModelClient{
  def apply(model:String) = new ModelClient(model)

  def models() = HttpClient.get[Vector[String]](Path.models)

}
