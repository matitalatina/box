package ch.wsl.box.model

import ch.wsl.box.jdbc.Connection
import ch.wsl.box.model.boxentities.Schema
import ch.wsl.box.jdbc.PostgresProfile.api._
import net.ceedubs.ficus.Ficus._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object DropBox extends App {

  println("Dropping box tables")

  def fut(db:Database) = for {
    _ <- db.run(Schema.box.dropIfExists)
  } yield true

  Await.result(fut(Connection.dbConnection),10 seconds)

}
