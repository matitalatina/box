package postgresweb.configs

import postgresweb.utils.Base64

import Base64._

/**
 * Created by andreaminetti on 23/02/16.
 */
object Config {

  def basicAuthToken(username: String, password: String):String = "Basic " + (username + ":" + password).getBytes.toBase64

  val endpoint = "http://localhost:8080/"

  val auth =  ("Authorization" -> basicAuthToken("andreaminetti",""))

}