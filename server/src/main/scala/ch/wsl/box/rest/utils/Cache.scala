package ch.wsl.box.rest.utils

import ch.wsl.box.jdbc.Connection
import ch.wsl.box.rest.logic.functions.RuntimeFunction
import ch.wsl.box.rest.metadata.{EntityMetadataFactory, FormMetadataFactory}

import scala.concurrent.ExecutionContext

object Cache {
  def reset()(implicit ec:ExecutionContext): Unit = {
    FormMetadataFactory.resetCache()
    EntityMetadataFactory.resetCache()
    RuntimeFunction.resetCache()
    BoxConfig.load(Connection.adminDB)
  }
}
