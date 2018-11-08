package ch.wsl.box.rest

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, failWith}
import akka.http.scaladsl.server.ExceptionHandler
import ch.wsl.box.model.shared.errors.{JsonDecoderExceptionReport, SQLExceptionReport}
import ch.wsl.box.rest.logic.JSONDecoderException
import ch.wsl.box.rest.utils.JSONSupport
import org.postgresql.util.PSQLException

object BoxExceptionHandler {

  import JSONSupport._
  import ch.wsl.box.shared.utils.Formatters._
  import io.circe.syntax._
  import io.circe.generic.auto._
  import ch.wsl.box.shared.utils.JSONUtils._

  def psql2sqlReport(sql: PSQLException):SQLExceptionReport = {
    val result  = SQLExceptionReport(
      Option(sql.getServerErrorMessage.getSchema),
      Option(sql.getServerErrorMessage.getTable),
      Option(sql.getServerErrorMessage.getColumn),
      Option(sql.getServerErrorMessage.getConstraint),
      Option(sql.getServerErrorMessage.getDetail),
      Option(sql.getServerErrorMessage.getHint),
      Option(sql.getServerErrorMessage.getInternalQuery),
      Option(sql.getServerErrorMessage.getMessage)
    )
    result
  }

  def apply()  = ExceptionHandler {
    case sql: PSQLException => {
      complete(StatusCodes.InternalServerError,psql2sqlReport(sql))
    }
    case JSONDecoderException(failure,json) => {
      import io.circe.CursorOp._
      val fields = failure.history.flatMap{
        case DownField(field) => Some(field)
        case _ => None
      }
      val cursors = failure.history.map(showCursorOp.show)
      val report = JsonDecoderExceptionReport(fields,cursors,failure.message,Some(json))
      complete(StatusCodes.InternalServerError,report)
    }
    case e: Exception => {
      e.printStackTrace();
      failWith(e)
    }
  }
}
