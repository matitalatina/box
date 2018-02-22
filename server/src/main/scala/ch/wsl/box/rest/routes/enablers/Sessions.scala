package ch.wsl.box.rest.routes.enablers

import ch.wsl.box.rest.utils.BoxSession
import com.softwaremill.session.SessionDirectives.setSession
import com.softwaremill.session.{InMemoryRefreshTokenStorage, SessionConfig, SessionManager}
import com.softwaremill.session.SessionOptions.{oneOff, usingCookies}

trait Sessions {
  val sessionConfig = SessionConfig.fromConfig()
  implicit val sessionManager = new SessionManager[BoxSession](sessionConfig)
  implicit val refreshTokenStorage = new InMemoryRefreshTokenStorage[BoxSession] {
    override def log(msg: String): Unit = {}
  }

  def boxSetSession(v: BoxSession) = setSession(oneOff, usingCookies, v)
}
