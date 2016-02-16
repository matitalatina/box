package ch.wsl.rest.domain

import slick.lifted.{AbstractTable, TableQuery}
import scala.reflect.runtime.universe._
import slick.driver.PostgresDriver.api._

/**
 * Created by andreaminetti on 16/02/16.
 */
object EnhancedTable {

  implicit class EnTable[T](t: Table[_]) {

    private val rm = scala.reflect.runtime.currentMirror
    private def accessor(field:String) = rm.classSymbol(t.getClass).toType.members.collectFirst{
      case m: MethodSymbol if m.name.toString == field => m
    }.get

    def col(field: String):Rep[_] = rm.reflect(t).reflectMethod(accessor(field)).apply().asInstanceOf[Rep[_]]


  }

}