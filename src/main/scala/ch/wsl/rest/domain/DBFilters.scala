package ch.wsl.rest.domain

import scala.slick.driver.PostgresDriver.simple._

trait DBFilters {
  def equality(c:Column[_],v:Any):Column[Option[Boolean]] 
}

trait UglyDBFilters extends DBFilters {
  def equality(c:Column[_],v:Any):Column[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]
      
    
      c.tpe.classTag.toString match {
          case "Short" => c.asInstanceOf[Column[Short]] === v.asInstanceOf[Int].toShort
          case "Int" => c.asInstanceOf[Column[Int]] === v.asInstanceOf[Int]
          case "String" => c.asInstanceOf[Column[String]] === v.asInstanceOf[String]
          case "scala.Option" => {
            c.tpe.children.headOption.map(_.classTag.toString) match {
              case Some("Short") => c.asInstanceOf[Column[Option[Short]]] === v.asInstanceOf[Int].toShort
              case Some("Int") => c.asInstanceOf[Column[Option[Int]]] === v.asInstanceOf[Int]
              case Some("java.lang.String") => c.asInstanceOf[Column[Option[String]]] === v.asInstanceOf[String]
              case _ => {
                println(c.tpe.children.headOption.map(_.classTag.toString))
                None
              }
            }
          }
          case _ => {
            println(c.tpe.classTag)
            None
          }
      }
   }
}