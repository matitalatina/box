package ch.wsl.box.rest.logic


import scribe.Logging
import slick.lifted.{AbstractTable, TableQuery}

import scala.reflect.runtime.universe._
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.model.shared.JSONID

import scala.concurrent.ExecutionContext




/**
 * Created by andreaminetti on 16/02/16.
 *
 * to retrieve the instance of a column
 */


class EnhancedModel(m:Product) extends Logging {



    private val rm = scala.reflect.runtime.currentMirror

    private def accessor(field:String):TermSymbol = {
      try {

        rm.classSymbol(m.getClass).toType.members.collectFirst {
          case m: TermSymbol if m.name.toString == field => m
        }.get
      } catch {
        case e: Exception => {
          logger.debug(rm.classSymbol(m.getClass).toType.members.toString)
          throw new Exception(s"Field not found:$field available fields: ${rm.classSymbol(m.getClass).toType.members}")
        }
      }
    }


    def ID(fields:Seq[String]):JSONID = {
      val values = fields map { field =>
        field -> {
          rm.reflect(m).reflectField(accessor(field)).get match {
            case opt: Option[_] => opt.map(_.toString).getOrElse("")
            case o: Any => o.toString()
          }
        }
      }
      JSONID.fromMap(values.toMap)
    }




}

