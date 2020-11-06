package ch.wsl.box.rest.runtime

import ch.wsl.box.rest.logic.{TableActions, ViewActions}
import io.circe.Json

import scala.concurrent.ExecutionContext

trait ActionRegistry {
  def apply(name:String)(implicit ec: ExecutionContext): TableActions[Json]
}
