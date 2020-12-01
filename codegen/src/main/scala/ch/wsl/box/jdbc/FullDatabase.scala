package ch.wsl.box.jdbc

import ch.wsl.box.jdbc.PostgresProfile.api._


case class FullDatabase(
                        db:Database,
                        adminDb:Database
                      ) {
  implicit val runDb = db
}