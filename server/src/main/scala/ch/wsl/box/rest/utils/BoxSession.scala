package ch.wsl.box.rest.utils

import ch.wsl.box.model.shared.LoginRequest
import com.softwaremill.session.{SessionSerializer, SingleValueSessionSerializer}

import scala.util.Try

case class BoxSession(username:String,password:String) {
  def userProfile = Auth.getUserProfile(username,password)
}

object BoxSession {
  implicit def serializer: SessionSerializer[BoxSession, String] = new SingleValueSessionSerializer(
    s => s"${s.username}::${s.password}",
    (un: String) => Try {
      val tokens = un.split("::")
      BoxSession(tokens(0),tokens(1))
    })

  def fromLogin(request:LoginRequest) = BoxSession(request.username,request.password)
}