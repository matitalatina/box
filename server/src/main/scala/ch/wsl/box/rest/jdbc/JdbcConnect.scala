package ch.wsl.box.rest.jdbc

import java.sql._

import io.circe.Json
import scribe.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import slick.driver.PostgresDriver.api._


/**
  * A Scala JDBC connection example by Alvin Alexander,
  * http://alvinalexander.com
  */
object JdbcConnect extends Logging {

  import ch.wsl.box.shared.utils.JsonUtils._
  import io.circe.syntax._

  case class SQLFunctionResult(headers:Seq[String],rows:Seq[Seq[Json]])

  def function(name:String,args: Seq[Json])(implicit ec:ExecutionContext,db:Database):Future[Option[SQLFunctionResult]] = {

    Future{
      // make the connection
      val connection = db.source.createConnection()
      val result = Try {
        // create the statement, and run the select query
        val statement = connection.createStatement()
        val query = s"SELECT * FROM $name(${args.map(_.toString()).mkString(",")})".replaceAll("'","\\'").replaceAll("\"","'")
        logger.info(query)
        val resultSet = statement.executeQuery(query)
        val metadata = getColumnMeta(resultSet.getMetaData)
        val data = getResults(resultSet,metadata)
        SQLFunctionResult(metadata.map(_.label),data)
      }.toOption
      connection.close()
      result
    }

  }

  private case class ColumnMeta(index: Int, label: String, datatype: String)


  /**
    * Returns a list of columns for specified ResultSet which describes column properties we are interested in.
    */
  private def getColumnMeta(rsMeta: ResultSetMetaData): Seq[ColumnMeta] =
    (for {
      idx <- 1 to rsMeta.getColumnCount
      colName = rsMeta.getColumnLabel(idx).toLowerCase
      colType = rsMeta.getColumnClassName(idx)
    } yield ColumnMeta(idx, colName, colType)).toList

  /**
    * Creates a stream of results on top of a ResultSet.
    */
  private def getResults(rs: ResultSet, cols: Seq[ColumnMeta]): Seq[Seq[Json]] =
    new Iterator[Seq[Json]] {
      def hasNext = rs.next
      def next() = rowToObj(rs,cols)
    }.toStream.toList

  /**
    * Given a row from a ResultSet produces a JSON document.
    */
  private def rowToObj(rs: ResultSet, cols: Seq[ColumnMeta]):Seq[Json] = {
    for {
      ColumnMeta(index, label, datatype) <- cols
      value = columnValueGetter(datatype, index, rs)
    } yield value
  }

  /**
    * Takes a fully qualified Java type as String and returns one of the subtypes of JValue by fetching a value
    * from result set and converting it to proper type.
    * It supports only the most common types and everything else that does not match this conversion is converted
    * to String automatically. If you see that you results should contain more specific type instead of String
    * add conversion cases to {{{resultsetGetters}}} map.
    */
  private def columnValueGetter(datatype: String, columnIdx: Int, rs: ResultSet): Json = {
    val obj = rs.getObject(columnIdx)
    if (obj == null)
      Json.Null
    else {
      resultsetGetters(datatype,obj)
    }
  }

  private def resultsetGetters(datatype:String,obj:Object):Json = datatype match {
    case "java.lang.Integer" => obj.asInstanceOf[Int].asJson
    case "java.lang.Long" => obj.asInstanceOf[Long].asJson
    case "java.lang.Double" => obj.asInstanceOf[Double].asJson
    case "java.lang.Float" => obj.asInstanceOf[Float].asJson
    case "java.lang.Boolean" =>  obj.asInstanceOf[Boolean].asJson
    case "java.sql.Clob" => {
      val clob = obj.asInstanceOf[Clob]
      clob.getSubString(1, clob.length.toInt).asJson
    }
    case "java.lang.String" => obj.asInstanceOf[String].asJson
    case _ => {
      logger.warn(s"datatype: $datatype not found")
      obj.toString.asJson
    }
    }
}
