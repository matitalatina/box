package ch.wsl.box.client.utils

import ch.wsl.box.model.shared.{JSONQuery, KeyList}

/**
  * Created by andre on 5/24/2017.
  */
object Session {

  import io.circe._
  import io.circe.syntax._
  import io.circe.generic.auto._
  import io.circe.parser._

  private final val QUERY = "query"
  private final val KEYS = "keys"

  def set[T](key:String,obj:T)(implicit encoder: Encoder[T]) = {
    println(s"Setting $key")
    org.scalajs.dom.window.sessionStorage.setItem(key,obj.asJson.toString())
  }

  def get[T](key:String)(implicit decoder: Decoder[T]):Option[T] = {
    val raw = org.scalajs.dom.window.sessionStorage.getItem(key)
    for{
      json <- parse(raw).right.toOption
      query <- json.as[T].right.toOption
    } yield query
  }

  def setQuery(query: JSONQuery) = set(QUERY,query)
  def getQuery():Option[JSONQuery] = get[JSONQuery](QUERY)


  def getKeys():Option[KeyList] = get[KeyList](KEYS)
  def setKeys(list:KeyList) = set(KEYS,list)
}
