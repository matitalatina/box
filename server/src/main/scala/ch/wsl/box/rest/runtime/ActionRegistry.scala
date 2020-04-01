package ch.wsl.box.rest.runtime

import ch.wsl.box.rest.logic.{EntityJSONTableActions, EntityJSONViewActions}

import scala.concurrent.ExecutionContext

trait ActionRegistry {
  def tableActions(implicit ec: ExecutionContext):String => EntityJSONTableActions
  def viewActions(implicit ec: ExecutionContext):String => Option[EntityJSONViewActions]
  def actions(name:String)(implicit ec: ExecutionContext):Option[EntityJSONViewActions]
}
