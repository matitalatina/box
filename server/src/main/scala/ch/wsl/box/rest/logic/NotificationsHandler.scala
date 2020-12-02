package ch.wsl.box.rest.logic

import java.sql.Connection

import ch.wsl.box.jdbc.PostgresProfile.api._
import org.postgresql.PGConnection

trait PgNotifier{
  def stop()
}

object NotificationsHandler {

  def create(db:Database,channel:String,callback: () => Unit):PgNotifier = new PgNotifier {
    val listener = new Listener(db.source.createConnection(),channel,callback)
    listener.start()
    override def stop(): Unit = listener.stopRunning()
  }

}

import java.sql.SQLException

class Listener(conn: Connection,channel:String,callback: () => Unit) extends Thread {
  private var running = true
  def stopRunning() = {
    running = false
  }
  private val stmt = conn.createStatement
  stmt.execute(s"LISTEN $channel")
  stmt.close
  private val pgconn:PGConnection = conn.unwrap(classOf[PGConnection])

  override def run(): Unit = {
    try while ( running ) {
      // notifications = pgconn.getNotifications
      // If this thread is the only one that uses the connection, a timeout can be used to
      // receive notifications immediately:
      val notifications = pgconn.getNotifications(10000)
      if(notifications != null) {
        notifications.foreach{ _ =>
          callback()
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