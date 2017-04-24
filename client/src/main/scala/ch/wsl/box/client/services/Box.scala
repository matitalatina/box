package ch.wsl.box.client.services

/**
  * Created by andre on 4/24/2017.
  */
object Box {

  private def auth = RestClient.basicAuthToken("postgres","")

  def models() = RestClient.server.models(auth)
}
