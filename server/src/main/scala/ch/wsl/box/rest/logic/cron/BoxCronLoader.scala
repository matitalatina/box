package ch.wsl.box.rest.logic.cron

import ch.wsl.box.jdbc.Connection
import ch.wsl.box.model.boxentities.BoxCron
import ch.wsl.box.jdbc.PostgresProfile.api._
import scribe.Logging

import scala.concurrent.ExecutionContext

class BoxCronLoader(cronScheduler:CronScheduler) extends Logging {


  def load()(implicit ec:ExecutionContext) = {
    Connection.adminDB.run{
      BoxCron.BoxCronTable.result
    }.map{_.map{ c =>
      logger.info(s"Add scheduler ${c.name} reccurring at ${c.cron}")
      cronScheduler.addSqlJob(SQLJob(c.name,c.cron,Connection.adminDB,sql"#${c.sql}".as[Boolean].head))
    }}
  }
}
