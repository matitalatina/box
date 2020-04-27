package ch.wsl.box.rest.utils.log

import scribe.{LogRecord, Logger}
import scribe.writer.Writer
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.model.boxentities.Log
import ch.wsl.box.model.boxentities.Log.Log_row

import scala.concurrent.{ExecutionContext, Future}

class DbWriter(db:Database)(implicit ec:ExecutionContext) extends Writer {
  override def write[M](record: LogRecord[M], output: String): Unit = {
    Logger.system.out.print(output)
    db.run{
      Log.table += Log_row(None,record.fileName,record.className,record.line.getOrElse(-1),record.message, record.timeStamp)
    }

  }
}
