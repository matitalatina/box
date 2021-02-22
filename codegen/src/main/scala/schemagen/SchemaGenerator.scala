package schemagen

import scala.concurrent.Await
import scala.concurrent.duration._

object SchemaGenerator {
  def run(): Unit = {
    Await.ready(ViewLabels.run(),30.seconds)
  }
}
