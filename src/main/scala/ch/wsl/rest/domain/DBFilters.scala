package ch.wsl.rest.domain

import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._

trait DBFilters {
  def ==(c:Col,v:Any):Rep[Option[Boolean]]
  def not(c:Col,v:Any):Rep[Option[Boolean]]
  def >(c:Col,v:Any):Rep[Option[Boolean]]
  def <(c:Col,v:Any):Rep[Option[Boolean]]
  def like(c:Col,v:Any):Rep[Option[Boolean]]
  
  def operator(op:String)(c:Col,v:Any) ={
    
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


  def ==(col:Col,value:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

      val v = value.toString
      val c:Rep[_] = col.rep

      col.`type` match {
          case "Short" => c.asInstanceOf[Rep[Short]] === v.asInstanceOf[String].toShort
          case "Int" | "java.lang.Integer" => c.asInstanceOf[Rep[Int]] === v.asInstanceOf[String].toInt
          case "Long" => c.asInstanceOf[Rep[Long]] === v.asInstanceOf[String].toLong
          case "java.lang.String" => c.asInstanceOf[Rep[String]] === v.asInstanceOf[String]
          case "Boolean" => c.asInstanceOf[Rep[Boolean]] === v.asInstanceOf[String].toBoolean
          case "scala.Option[Short]" =>  c.asInstanceOf[Rep[Option[Short]]] === v.asInstanceOf[String].toShort
          case "scala.Option[Int]" | "scala.Option[java.lang.Integer]" => c.asInstanceOf[Rep[Option[Int]]] === v.asInstanceOf[String].toInt
          case "scala.Option[Long]" => c.asInstanceOf[Rep[Option[Long]]] === v.asInstanceOf[String].toLong
          case "scala.Option[String]" => c.asInstanceOf[Rep[Option[String]]] === v.asInstanceOf[String]
          case "scala.Option[Boolean]" => c.asInstanceOf[Rep[Option[Boolean]]] === v.asInstanceOf[String].toBoolean
          case _ => {
                println("Type mapping for: " + col.`type`+ " not found")
                None
          }
      }
   }

  def not(col:Col,v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]


    val c:Rep[_] = col.rep

    col.`type` match {
          case "Short" => c.asInstanceOf[Rep[Short]] =!= v.asInstanceOf[String].toShort
          case "Int" => c.asInstanceOf[Rep[Int]] =!= v.asInstanceOf[String].toInt
          case "java.lang.String" => c.asInstanceOf[Rep[String]] =!= v.asInstanceOf[String]
          case "Boolean" => c.asInstanceOf[Rep[Boolean]] =!= v.asInstanceOf[String].toBoolean
          case "scala.Option[Short]" => c.asInstanceOf[Rep[Option[Short]]] =!= v.asInstanceOf[String].toShort
          case "scala.Option[Int]" => c.asInstanceOf[Rep[Option[Int]]] =!= v.asInstanceOf[String].toInt
          case "scala.Option[String]" => c.asInstanceOf[Rep[Option[String]]] =!= v.asInstanceOf[String]
          case "scala.Option[Boolean]" => c.asInstanceOf[Rep[Option[Boolean]]] =!= v.asInstanceOf[String].toBoolean
          case _ => {
            println("Type mapping for: " + col.`type` + " not found")
            None
          }
      }
   }

  def >(col:Col,v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]


    val c:Rep[_] = col.rep

    col.`type` match {
          case "Short" => c.asInstanceOf[Rep[Short]] > v.asInstanceOf[String].toShort
          case "Int" => c.asInstanceOf[Rep[Int]] > v.asInstanceOf[String].toInt
          case "scala.Option[Short]" => c.asInstanceOf[Rep[Option[Short]]] > v.asInstanceOf[String].toShort
          case "scala.Option[Int]" => c.asInstanceOf[Rep[Option[Int]]] > v.asInstanceOf[String].toInt
          case _ => {
              println("Type mapping for: " + col.`type` + " not found")
              None
          }
      }



   }

  def <(col:Col,v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]


    val c:Rep[_] = col.rep

    col.`type` match {
          case "Short" => c.asInstanceOf[Rep[Short]] < v.asInstanceOf[String].toShort
          case "Int" => c.asInstanceOf[Rep[Int]] < v.asInstanceOf[String].toInt
          case "scala.Option[Short]" => c.asInstanceOf[Rep[Option[Short]]] < v.asInstanceOf[String].toShort
          case "scala.Option[Int]" => c.asInstanceOf[Rep[Option[Int]]] < v.asInstanceOf[String].toInt
          case _ => {
            println("Type mapping for: " + col.`type` + " not found")
            None
          }
      }
   }

  def like(col:Col,v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    col.`type` match {
          case "java.lang.String" => c.asInstanceOf[Rep[String]] like v.asInstanceOf[String]
          case "scala.Option[String]" => c.asInstanceOf[Rep[Option[String]]] === v.asInstanceOf[String]
          case _ => {
            println("Type mapping for: " + col.`type` + " not found")
            None
          }
      }
   }

}