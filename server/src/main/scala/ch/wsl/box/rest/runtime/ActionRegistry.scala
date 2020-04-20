package ch.wsl.box.rest.runtime

import ch.wsl.box.rest.logic.{TableActions, ViewActions}
import io.circe.Json

import scala.concurrent.ExecutionContext

trait ActionRegistry {
  def tableActions(implicit ec: ExecutionContext):String => TableActions[Json]
  def viewActions(implicit ec: ExecutionContext):String => Option[ViewActions[Json]]
  def actions(name:String)(implicit ec: ExecutionContext):Option[ViewActions[Json]]
}
