package ch.wsl.box.client.utils

import ch.wsl.box.client.services.REST
import ch.wsl.box.model.shared.JSONQuery

import scala.concurrent.Future
import scalajs.concurrent.JSExecutionContext.Implicits.queue


/**
  * Created by andre on 5/24/2017.
  */

case class Navigation(hasNext:Boolean, hasPrevious:Boolean, count:Int, current:Int,
                      hasNextPage:Boolean, hasPreviousPage:Boolean, pages:Int, currentPage:Int)

object Navigation{
  def empty0 = Navigation(false,false,0,0, false, false, 0,0)
  def empty1 = Navigation(false,false,1,1, false, false, 1,1)
}

case class IdNav(currentId:Option[String]) {

  def navigation():Option[Navigation] = for {
    id <- currentId
    ids <- Session.getIDs()
    query <- Session.getQuery()
    (_,idx) <- ids.ids.zipWithIndex.find(_._1 == id)
  } yield {
    Navigation(
      hasNext(),
      hasPrev(),
      ids.count,
      (query.currentPage-1)*query.pageLength(ids.count)+idx+1,
      hasNextPage(),
      hasPrevPage(),
      ids.count % query.pageLength(ids.count) +1 ,
      query.currentPage

    )
  }


  def hasNext():Boolean = {
    for{
      id <- currentId
      ids <- Session.getIDs()
      (_,idx) <- ids.ids.zipWithIndex.find(_._1 == id)
    } yield {
      //      println("checking")
      val result = !(idx == ids.ids.size-1 && ids.isLastPage)
      //      println(s"result: $result")
      result
    }
  }.getOrElse(false)

  def hasPrev():Boolean = {
    val result = {
      for{
        id <- currentId
        query <- Session.getQuery()
        ids <- Session.getIDs()
        (_,idx) <- ids.ids.zipWithIndex.find(_._1 == id)
      } yield {
        !(idx == 0 && query.currentPage == 1)
      }
    }.getOrElse(false)
    result
  }

  def hasNextPage():Boolean = {
    for{
      ids <- Session.getIDs()
    } yield {
      !ids.isLastPage
    }
  }.getOrElse(false)

  def hasPrevPage():Boolean = {
      for{
        query <- Session.getQuery()
      } yield {
        !(query.currentPage == 1)
      }
  }.getOrElse(false)


  def prev(kind:String,model:String):Future[Option[String]] = {
    val data = for {
      id <- currentId
      query <- Session.getQuery()
      ids <- Session.getIDs()
      (_, idx) <- ids.ids.zipWithIndex.find(_._1 == id)
    } yield {
      (idx == 0,query.currentPage == 1) match {
        case (false,_) => Future.successful(ids.ids.lift(idx-1))
        case (true,false) => prevPage(kind,model,query)
        case (true,true) => Future.successful(None)
      }
    }
    data.getOrElse(Future.successful(None))
  }

  def next(kind:String,model:String):Future[Option[String]] = {
    val data = for {
      id <- currentId
      query <- Session.getQuery()
      ids <- Session.getIDs()
      (_, idx) <- ids.ids.zipWithIndex.find(_._1 == id)
    } yield {
      (idx == ids.ids.size-1,ids.isLastPage) match {
        case (false,_) => Future.successful(ids.ids.lift(idx+1))
        case (true,false) => nextPage(kind,model,query)
        case (true,true) => Future.successful(None)
      }
    }
    data.getOrElse(Future.successful(None))
  }


  def prevPage(kind:String,model:String,query: JSONQuery):Future[Option[String]] = if (query.currentPage==1) {
      Future.successful(None)
    }else {
      val newQuery = query.copy(paging = query.paging.map(p => p.copy(currentPage= p.currentPage - 1)))
      REST.ids(kind, Session.lang(), model, newQuery).map { ids =>
        Session.setQuery(newQuery)
        Session.setIDs(ids)
        ids.ids.lastOption
      }
    }

  def nextPage(kind:String,model:String,query: JSONQuery):Future[Option[String]] = {
    val newQuery = query.copy(paging = query.paging.map(p => p.copy(currentPage= p.currentPage + 1)))
    REST.ids(kind,Session.lang(),model,newQuery).map{ ids =>
      Session.setQuery(newQuery)
      Session.setIDs(ids)
      ids.ids.headOption
    }
  }


}
