package ch.wsl.box.testmodel

import scala.concurrent.ExecutionContext
import scala.util.Try
import ch.wsl.box.rest.logic.{JSONTableActions, JSONViewActions, TableActions, ViewActions}

import ch.wsl.box.rest.runtime._

object EntityActionsRegistry extends ActionRegistry {

  import Entities._
  import io.circe._
  import io.circe.generic.auto._
  import ch.wsl.box.rest.utils.JSONSupport._

  def tableActions(implicit ec: ExecutionContext) :String => TableActions[Json] = {
       case "child" => JSONTableActions[Child,Child_row](Child)
   case "parent" => JSONTableActions[Parent,Parent_row](Parent)
   case "simple" => JSONTableActions[Simple,Simple_row](Simple)
   case "subchild" => JSONTableActions[Subchild,Subchild_row](Subchild)
  }

  def viewActions(implicit ec: ExecutionContext) :String => Option[ViewActions[Json]] = {
    
    case _ => None
  }

  def actions(name:String)(implicit ec: ExecutionContext) : Option[ViewActions[Json]] = viewActions(ec)(name).orElse(Try(tableActions(ec)(name)).toOption)

}

           
