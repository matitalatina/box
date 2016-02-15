package ch.wsl.rest.domain

import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

trait DBFilters {
  def ==(c:Rep[_],v:Any):Rep[Option[Boolean]]
  def not(c:Rep[_],v:Any):Rep[Option[Boolean]]
  def >(c:Rep[_],v:Any):Rep[Option[Boolean]]
  def <(c:Rep[_],v:Any):Rep[Option[Boolean]]
  def like(c:Rep[_],v:Any):Rep[Option[Boolean]]
  
  def operator(op:String)(c:Rep[_],v:Any) ={
    
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


  def ==(c:Rep[_],v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]


      c.toNode.nodeType.classTag.toString match {
          case "Short" => c.asInstanceOf[Rep[Short]] === v.asInstanceOf[String].toShort
          case "Int" => c.asInstanceOf[Rep[Int]] === v.asInstanceOf[String].toInt
          case "java.lang.String" => c.asInstanceOf[Rep[String]] === v.asInstanceOf[String]
          case "Boolean" => c.asInstanceOf[Rep[Boolean]] === v.asInstanceOf[String].toBoolean
          case "scala.Option" => {
            c.toNode.children.headOption.map(_.nodeType.classTag.toString) match {
              case Some("Short") => c.asInstanceOf[Rep[Option[Short]]] === v.asInstanceOf[String].toShort
              case Some("Int") => c.asInstanceOf[Rep[Option[Int]]] === v.asInstanceOf[String].toInt
              case Some("java.lang.String") => c.asInstanceOf[Rep[Option[String]]] === v.asInstanceOf[String]
              case Some("Boolean") => c.asInstanceOf[Rep[Option[Boolean]]] === v.asInstanceOf[String].toBoolean
              case _ => {
                println("Type mapping for: " + c.toNode.children.headOption.map(_.nodeType.classTag.toString) + " not found")
                None
              }
            }
          }
          case _ => {
            println("Type mapping for: " + c.toNode.nodeType.classTag + " not found")
            None
          }
      }
   }

  def not(c:Rep[_],v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]


    c.toNode.nodeType.classTag.toString match {
          case "Short" => c.asInstanceOf[Rep[Short]] =!= v.asInstanceOf[String].toShort
          case "Int" => c.asInstanceOf[Rep[Int]] =!= v.asInstanceOf[String].toInt
          case "java.lang.String" => c.asInstanceOf[Rep[String]] =!= v.asInstanceOf[String]
          case "Boolean" => c.asInstanceOf[Rep[Boolean]] =!= v.asInstanceOf[String].toBoolean
          case "scala.Option" => {
            c.toNode.children.headOption.map(_.nodeType.classTag.toString) match {
              case Some("Short") => c.asInstanceOf[Rep[Option[Short]]] =!= v.asInstanceOf[String].toShort
              case Some("Int") => c.asInstanceOf[Rep[Option[Int]]] =!= v.asInstanceOf[String].toInt
              case Some("java.lang.String") => c.asInstanceOf[Rep[Option[String]]] =!= v.asInstanceOf[String]
              case Some("Boolean") => c.asInstanceOf[Rep[Option[Boolean]]] =!= v.asInstanceOf[String].toBoolean
              case _ => {
                println("Type mapping for: " + c.toNode.children.headOption.map(_.nodeType.classTag.toString) + " not found")
                None
              }
            }
          }
          case _ => {
            println("Type mapping for: " + c.toNode.nodeType.classTag + " not found")
            None
          }
      }
   }

  def >(c:Rep[_],v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]


      c.toNode.nodeType.classTag.toString match {
          case "Short" => c.asInstanceOf[Rep[Short]] > v.asInstanceOf[String].toShort
          case "Int" => c.asInstanceOf[Rep[Int]] > v.asInstanceOf[String].toInt
          case "scala.Option" => {
            c.toNode.children.headOption.map(_.nodeType.classTag.toString) match {
              case Some("Short") => c.asInstanceOf[Rep[Option[Short]]] > v.asInstanceOf[String].toShort
              case Some("Int") => c.asInstanceOf[Rep[Option[Int]]] > v.asInstanceOf[String].toInt
              case _ => {
                println("Type mapping for: " + c.toNode.children.headOption.map(_.nodeType.classTag.toString) + " not found")
                None
              }
            }
          }
          case _ => {
            println("Type mapping for: " + c.toNode.nodeType.classTag + " not found")
            None
          }
      }
   }

  def <(c:Rep[_],v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]


      c.toNode.nodeType.classTag.toString match {
          case "Short" => c.asInstanceOf[Rep[Short]] < v.asInstanceOf[String].toShort
          case "Int" => c.asInstanceOf[Rep[Int]] < v.asInstanceOf[String].toInt
          case "scala.Option" => {
            c.toNode.children.headOption.map(_.nodeType.classTag.toString) match {
              case Some("Short") => c.asInstanceOf[Rep[Option[Short]]] < v.asInstanceOf[String].toShort
              case Some("Int") => c.asInstanceOf[Rep[Option[Int]]] < v.asInstanceOf[String].toInt
              case _ => {
                println("Type mapping for: " + c.toNode.children.headOption.map(_.nodeType.classTag.toString) + " not found")
                None
              }
            }
          }
          case _ => {
            println("Type mapping for: " + c.toNode.nodeType.classTag + " not found")
            None
          }
      }
   }

  def like(c:Rep[_],v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

      c.toNode.nodeType.classTag.toString match {
          case "java.lang.String" => c.asInstanceOf[Rep[String]] like v.asInstanceOf[String]
          case "scala.Option" => {
            c.toNode.children.headOption.map(_.nodeType.classTag.toString) match {
              case Some("java.lang.String") => c.asInstanceOf[Rep[Option[String]]] like v.asInstanceOf[String]
              case _ => {
                println("Type mapping for: " + c.toNode.children.headOption.map(_.nodeType.classTag.toString) + " not found")
                None
              }
            }
          }
          case _ => {
            println("Type mapping for: " + c.toNode.nodeType.classTag + " not found")
            None
          }
      }
   }

}