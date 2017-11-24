package ch.wsl.box.rest.logic

import slick.lifted.{AbstractTable, TableQuery}
import scala.reflect.runtime.universe._
import slick.driver.PostgresDriver.api._


case class Col(rep:Rep[_],`type`:String)

/**
 * Created by andreaminetti on 16/02/16.
  *
  * to retrieve the instance of a column
 */


object EnhancedTable {

  implicit class EnTable[T](t: Table[_]) {


    private val rm = scala.reflect.runtime.currentMirror

    private def accessor(field:String):MethodSymbol = {
      try {

          rm.classSymbol(t.getClass).toType.members.collectFirst {
            case m: MethodSymbol if m.name.toString == field => m
          }.get
        } catch {
          case e: Exception => {
            println(rm.classSymbol(t.getClass).toType.members)
            throw new Exception(s"Field not found:$field available fields: ${rm.classSymbol(t.getClass).toType.members} of table:${t.tableName}")
          }
        }
      }


    def col(field: String):Col = Col(
      rm.reflect(t).reflectMethod(accessor(field)).apply().asInstanceOf[slick.driver.PostgresDriver.api.Rep[_]],
      typ(field)
    )

    def typ(field:String) = {
      //=> slick.driver.PostgresDriver.api.Rep[scala.Option[String]]
      accessor(field).info.toString.stripPrefix("=> slick.driver.PostgresDriver.api.Rep[").stripSuffix("]")
    }



  }

}