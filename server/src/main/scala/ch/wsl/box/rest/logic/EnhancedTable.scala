package ch.wsl.box.rest.logic

import scribe.Logging
import slick.lifted.{AbstractTable, TableQuery}

import scala.reflect.runtime.universe._
import ch.wsl.box.rest.jdbc.PostgresProfile.api._


case class Col(rep:Rep[_],`type`:String)

/**
 * Created by andreaminetti on 16/02/16.
  *
  * to retrieve the instance of a column
 */


object EnhancedTable extends Logging {

  implicit class EnTable[T](t: Table[_]) {


    private val rm = scala.reflect.runtime.currentMirror

    private def accessor(field:String):MethodSymbol = {
      try {

          rm.classSymbol(t.getClass).toType.members.collectFirst {
            case m: MethodSymbol if m.name.toString == field => m
          }.get
        } catch {
          case e: Exception => {
            logger.debug(rm.classSymbol(t.getClass).toType.members.toString)
            throw new Exception(s"Field not found:$field available fields: ${rm.classSymbol(t.getClass).toType.members} of table:${t.tableName}")
          }
        }
      }


    def col(field: String):Col = Col(
      rm.reflect(t).reflectMethod(accessor(field)).apply().asInstanceOf[ch.wsl.box.rest.jdbc.PostgresProfile.api.Rep[_]],
      typ(field)
    )

    def typ(field:String) = {
      //=> ch.wsl.box.rest.jdbc.PostgresProfile.api.Rep[scala.Option[String]]
      accessor(field).info.toString.stripPrefix("=> ch.wsl.box.rest.jdbc.PostgresProfile.api.Rep[").stripSuffix("]")
    }

    def cols(fields: Seq[String]):Seq[Col] = fields.map(col(_))

    def reps(fields: Seq[String]):Seq[Rep[_]] = fields.map(col(_).rep)


  }

}

