package ch.wsl.rest.domain

import scala.slick.driver.PostgresDriver.simple._

trait DBFilters {
  def ==(c:Column[_],v:Any):Column[Option[Boolean]] 
  def not(c:Column[_],v:Any):Column[Option[Boolean]]
  def >(c:Column[_],v:Any):Column[Option[Boolean]]
  def <(c:Column[_],v:Any):Column[Option[Boolean]]
  def like(c:Column[_],v:Any):Column[Option[Boolean]] 
  
  def operator(op:String)(c:Column[_],v:Any) ={
    
    println("operator: " + op)
    
    op match{
      case "="     => ==(c, v)
      case "not"    => not(c, v)
      case ">"      => >(c, v)
      case "<"      => <(c, v)
      case "like"   => like(c, v)
    }
  }
  
}

trait UglyDBFilters extends DBFilters {
  def ==(c:Column[_],v:Any):Column[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]
      
    
      c.tpe.classTag.toString match {
          case "Short" => c.asInstanceOf[Column[Short]] === v.asInstanceOf[String].toShort
          case "Int" => c.asInstanceOf[Column[Int]] === v.asInstanceOf[String].toInt
          case "java.lang.String" => c.asInstanceOf[Column[String]] === v.asInstanceOf[String]
          case "Boolean" => c.asInstanceOf[Column[Boolean]] === v.asInstanceOf[String].toBoolean
          case "scala.Option" => {
            c.tpe.children.headOption.map(_.classTag.toString) match {
              case Some("Short") => c.asInstanceOf[Column[Option[Short]]] === v.asInstanceOf[String].toShort
              case Some("Int") => c.asInstanceOf[Column[Option[Int]]] === v.asInstanceOf[String].toInt
              case Some("java.lang.String") => c.asInstanceOf[Column[Option[String]]] === v.asInstanceOf[String]
              case Some("Boolean") => c.asInstanceOf[Column[Option[Boolean]]] === v.asInstanceOf[String].toBoolean
              case _ => {
                println("Type mapping for: " + c.tpe.children.headOption.map(_.classTag.toString) + " not found") 
                None
              }
            }
          }
          case _ => {
            println("Type mapping for: " + c.tpe.classTag + " not found") 
            None
          }
      }
   }
  
  def not(c:Column[_],v:Any):Column[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]
      
    
      c.tpe.classTag.toString match {
          case "Short" => c.asInstanceOf[Column[Short]] =!= v.asInstanceOf[String].toShort
          case "Int" => c.asInstanceOf[Column[Int]] =!= v.asInstanceOf[String].toInt
          case "java.lang.String" => c.asInstanceOf[Column[String]] =!= v.asInstanceOf[String]
          case "Boolean" => c.asInstanceOf[Column[Boolean]] =!= v.asInstanceOf[String].toBoolean
          case "scala.Option" => {
            c.tpe.children.headOption.map(_.classTag.toString) match {
              case Some("Short") => c.asInstanceOf[Column[Option[Short]]] =!= v.asInstanceOf[String].toShort
              case Some("Int") => c.asInstanceOf[Column[Option[Int]]] =!= v.asInstanceOf[String].toInt
              case Some("java.lang.String") => c.asInstanceOf[Column[Option[String]]] =!= v.asInstanceOf[String]
              case Some("Boolean") => c.asInstanceOf[Column[Option[Boolean]]] =!= v.asInstanceOf[String].toBoolean
              case _ => {
                println("Type mapping for: " + c.tpe.children.headOption.map(_.classTag.toString) + " not found") 
                None
              }
            }
          }
          case _ => {
            println("Type mapping for: " + c.tpe.classTag + " not found") 
            None
          }
      }
   }
  
  def >(c:Column[_],v:Any):Column[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]
      
    
      c.tpe.classTag.toString match {
          case "Short" => c.asInstanceOf[Column[Short]] > v.asInstanceOf[String].toShort
          case "Int" => c.asInstanceOf[Column[Int]] > v.asInstanceOf[String].toInt
          case "scala.Option" => {
            c.tpe.children.headOption.map(_.classTag.toString) match {
              case Some("Short") => c.asInstanceOf[Column[Option[Short]]] > v.asInstanceOf[String].toShort
              case Some("Int") => c.asInstanceOf[Column[Option[Int]]] > v.asInstanceOf[String].toInt
              case _ => {
                println("Type mapping for: " + c.tpe.children.headOption.map(_.classTag.toString) + " not found") 
                None
              }
            }
          }
          case _ => {
            println("Type mapping for: " + c.tpe.classTag + " not found") 
            None
          }
      }
   }
  
  def <(c:Column[_],v:Any):Column[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]
      
    
      c.tpe.classTag.toString match {
          case "Short" => c.asInstanceOf[Column[Short]] < v.asInstanceOf[String].toShort
          case "Int" => c.asInstanceOf[Column[Int]] < v.asInstanceOf[String].toInt
          case "scala.Option" => {
            c.tpe.children.headOption.map(_.classTag.toString) match {
              case Some("Short") => c.asInstanceOf[Column[Option[Short]]] < v.asInstanceOf[String].toShort
              case Some("Int") => c.asInstanceOf[Column[Option[Int]]] < v.asInstanceOf[String].toInt
              case _ => {
                println("Type mapping for: " + c.tpe.children.headOption.map(_.classTag.toString) + " not found") 
                None
              }
            }
          }
          case _ => {
            println("Type mapping for: " + c.tpe.classTag + " not found") 
            None
          }
      }
   }
  
  def like(c:Column[_],v:Any):Column[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]
      
      c.tpe.classTag.toString match {
          case "java.lang.String" => c.asInstanceOf[Column[String]] like v.asInstanceOf[String]
          case "scala.Option" => {
            c.tpe.children.headOption.map(_.classTag.toString) match {
              case Some("java.lang.String") => c.asInstanceOf[Column[Option[String]]] like v.asInstanceOf[String]
              case _ => {
                println("Type mapping for: " + c.tpe.children.headOption.map(_.classTag.toString) + " not found") 
                None
              }
            }
          }
          case _ => {
            println("Type mapping for: " + c.tpe.classTag + " not found") 
            None
          }
      }
   }
}