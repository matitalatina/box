package ch.wsl.box.rest.logic.notification

import java.sql.Connection
import java.util.Date

import ch.wsl.box.rest.utils.Auth
import org.postgresql.PGConnection
import scribe.Logging

trait PgNotifier{
  def stop()
}

object NotificationsHandler {

  def create(channel:String,callback: (String) => Unit):PgNotifier = new PgNotifier {
    val listener = new Listener(Auth.dbConnection.source.createConnection(),channel,callback)
    listener.start()
    override def stop(): Unit = listener.stopRunning()
  }

}

import java.sql.SQLException

class Listener(conn: Connection,channel:String,callback: (String) => Unit) extends Thread with Logging {
  private var running = true
  def stopRunning() = {
    running = false
  }
  private val stmt = conn.createStatement
  val listenQuery = s"SET ROLE ${Auth.adminUser}; LISTEN $channel"
  logger.info(listenQuery)
  stmt.execute(listenQuery)
  stmt.close
  private val pgconn:PGConnection = conn.unwrap(classOf[PGConnection])

  override def run(): Unit = {
    try while ( running ) {
      // notifications = pgconn.getNotifications
      // If this thread is the only one that uses the connection, a timeout can be used to
      // receive notifications immediately:
      val notifications = pgconn.getNotifications(10000)
      if(notifications != null) {
        notifications.foreach{ n =>
          logger.info(s"""
             |Recived notification:
             |timestamp: ${new Date().toString}
             |name: ${n.getName}
             |parameter: ${n.getParameter}
             |""".stripMargin)
          callback(n.getParameter)
        }
      }
      // wait a while before checking again for new
      // notifications
      //Thread.sleep(500)
    }
    catch {
      case sqle: SQLException =>
        sqle.printStackTrace()
      case ie: InterruptedException =>
        ie.printStackTrace()
    }
  }
}