package ch.wsl.rest.domain

/**
  * Created by andreaminetti on 03/03/16.
  */
case class JSONResult[M](count:Int,data:List[M])

case class JSONCount(count:Int)