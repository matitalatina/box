package ch.wsl.box.jdbc

import ch.wsl.box.jdbc.PostgresProfile.api._


case class FullDatabase(
                        db:UserDatabase,
                        adminDb:UserDatabase
                      ) {
  implicit val runDb = db
}