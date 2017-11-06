package ch.wsl.box.client.utils

import ch.wsl.box.client.services.REST
import ch.wsl.box.model.shared.JSONQuery

import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue


/**
  * Created by andre on 5/24/2017.
  */

case class Navigation(hasNext:Boolean, hasPrevious:Boolean, count:Int, current:Int)

case class IDSequence(idOpt:Option[String]) {
  def hasNext():Boolean = {
    for{
      id <- idOpt
      list <- Session.getKeys()
      (_,idx) <- list.keys.zipWithIndex.find(_._1 == id)
    } yield {
      println("checking")
      val result = !(idx == list.keys.size-1 && list.last)
      println(s"result: $result")
      result
    }
  }.getOrElse(false)

  def navigation():Option[Navigation] = for {
    id <- idOpt
    list <- Session.getKeys()
    query <- Session.getQuery()
    (_,idx) <- list.keys.zipWithIndex.find(_._1 == id)
  } yield {
    Navigation(
      hasNext(),
      hasPrev(),
      list.count,
      (query.page-1)*query.paging.map(_.count).getOrElse(list.count)+idx+1
    )
  }


  def hasPrev():Boolean = {
    val result = {
      for{
        id <- idOpt
        query <- Session.getQuery()
        list <- Session.getKeys()
        (_,idx) <- list.keys.zipWithIndex.find(_._1 == id)
      } yield {
        !(idx == 0 && query.page == 1)
      }
    }.getOrElse(false)
    result
  }


  def prevPage(kind:String,model:String,query: JSONQuery):Future[Option[String]] = if (query.page==1) {
      Future.successful(None)
    }else {
      val newQuery = query.copy(paging = query.paging.map(p => p.copy(page= p.page - 1)))
      REST.keysList(kind, Session.lang(), model, newQuery).map { keys =>
        Session.setQuery(newQuery)
        Session.setKeys(keys)
        keys.keys.lastOption
      }
    }

  def nextPage(kind:String,model:String,query: JSONQuery):Future[Option[String]] = {
    val newQuery = query.copy(paging = query.paging.map(p => p.copy(page= p.page + 1)))
    REST.keysList(kind,Session.lang(),model,newQuery).map{ keys =>
      Session.setQuery(newQuery)
      Session.setKeys(keys)
      keys.keys.headOption
    }
  }

  def prev(kind:String,model:String):Future[Option[String]] = {
      val result = for {
        id <- idOpt
        query <- Session.getQuery()
        list <- Session.getKeys()
        (_, idx) <- list.keys.zipWithIndex.find(_._1 == id)
      } yield {
        (idx == 0,query.page == 1) match {
          case (false,_) => Future.successful(list.keys.lift(idx-1))
          case (true,false) => prevPage(kind,model,query)
          case (true,true) => Future.successful(None)
        }
      }
      result.getOrElse(Future.successful(None))
  }

  def next(kind:String,model:String):Future[Option[String]] = {
    val result = for {
      id <- idOpt
      query <- Session.getQuery()
      list <- Session.getKeys()
      (_, idx) <- list.keys.zipWithIndex.find(_._1 == id)
    } yield {
      (idx == list.keys.size-1,list.last) match {
        case (false,_) => Future.successful(list.keys.lift(idx+1))
        case (true,false) => nextPage(kind,model,query)
        case (true,true) => Future.successful(None)
      }
    }
    result.getOrElse(Future.successful(None))
  }
}
