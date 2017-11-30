package ch.wsl.box.rest.logic

import java.sql.Timestamp
import java.text.SimpleDateFormat

import ch.wsl.box.model.shared.Filter
import slick.driver.PostgresDriver.api._

trait DBFilters {
  def ==(c:Col,v:Any):Rep[Option[Boolean]]
  def not(c:Col,v:Any):Rep[Option[Boolean]]
  def >(c:Col, v:Any):Rep[Option[Boolean]]
  def <(c:Col, v:Any):Rep[Option[Boolean]]
  def >=(c:Col, v:Any):Rep[Option[Boolean]]
  def <=(c:Col, v:Any):Rep[Option[Boolean]]
  def like(c:Col,v:Any):Rep[Option[Boolean]]
  def in(c:Col,v:Any):Rep[Option[Boolean]]
  def between(c:Col,v:Any):Rep[Option[Boolean]]

  def operator(op:String)(c:Col,v:Any) ={


    op match{
      case Filter.EQUALS  => ==(c, v)
      case Filter.NOT     => not(c, v)
      case Filter.>       => >(c, v)
      case Filter.<       => <(c, v)
      case Filter.>=      => >=(c, v)
      case Filter.<=      => <=(c, v)
      case Filter.LIKE    => like(c, v)
      case Filter.IN      => in(c, v)
      case Filter.BETWEEN   => between(c, v)
    }
  }

}

trait UglyDBFilters extends DBFilters {

  val timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")  //attention the format is different to that in the client for datetimepicker

  def ==(col:Col,value:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

      val v = value.toString
      val c:Rep[_] = col.rep

      col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] === v.asInstanceOf[String].toShort
          case "Double" | "scala.Double" => c.asInstanceOf[Rep[Double]] === v.asInstanceOf[String].toDouble
          case "scala.Int" | "java.lang.Integer" | "Int" => c.asInstanceOf[Rep[Int]] === v.asInstanceOf[String].toInt
          case "scala.Long" | "Long" => c.asInstanceOf[Rep[Long]] === v.asInstanceOf[String].toLong
          case "String" => c.asInstanceOf[Rep[String]] === v.asInstanceOf[String]
          case "scala.Boolean" => c.asInstanceOf[Rep[Boolean]] === v.asInstanceOf[String].toBoolean
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] === new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case "scala.Option[scala.Short]" =>  c.asInstanceOf[Rep[Option[Short]]] === v.asInstanceOf[String].toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] === v.asInstanceOf[String].toDouble
          case "scala.Option[scala.Int]" | "scala.Option[java.lang.Integer]" => c.asInstanceOf[Rep[Option[Int]]] === v.asInstanceOf[String].toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] === v.asInstanceOf[String].toLong
          case "scala.Option[String]" => c.asInstanceOf[Rep[Option[String]]] === v.asInstanceOf[String]
          case "scala.Option[scala.Boolean]" => c.asInstanceOf[Rep[Option[Boolean]]] === v.asInstanceOf[String].toBoolean
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] === new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case _ => {
                println("Type mapping for: " + col.`type`+ " not found")
                None
          }
      }
   }

  def not(col:Col,v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]


    val c:Rep[_] = col.rep

    col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] =!= v.asInstanceOf[String].toShort
          case "scala.Int" | "Int" => c.asInstanceOf[Rep[Int]] =!= v.asInstanceOf[String].toInt
          case "scala.Long" | "Int" => c.asInstanceOf[Rep[Long]] =!= v.asInstanceOf[String].toLong
          case "Double" | "scala.Double" => c.asInstanceOf[Rep[Double]] =!= v.asInstanceOf[String].toDouble
          case "String" => c.asInstanceOf[Rep[String]] =!= v.asInstanceOf[String]
          case "scala.Boolean" => c.asInstanceOf[Rep[Boolean]] =!= v.asInstanceOf[String].toBoolean
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] =!= new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case "scala.Option[scala.Short]" => c.asInstanceOf[Rep[Option[Short]]] =!= v.asInstanceOf[String].toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] =!= v.asInstanceOf[String].toDouble
          case "scala.Option[scala.Int]" => c.asInstanceOf[Rep[Option[Int]]] =!= v.asInstanceOf[String].toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] =!= v.asInstanceOf[String].toLong
          case "scala.Option[String]" => c.asInstanceOf[Rep[Option[String]]] =!= v.asInstanceOf[String]
          case "scala.Option[scala.Boolean]" => c.asInstanceOf[Rep[Option[Boolean]]] =!= v.asInstanceOf[String].toBoolean
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] =!= new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case _ => {
            println("Type mapping for: " + col.`type` + " not found")
            None
          }
      }
   }

  def >(col:Col, v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] > v.asInstanceOf[String].toShort
          case "scala.Double" | "Double" => c.asInstanceOf[Rep[Double]] > v.asInstanceOf[String].toDouble
          case "scala.Int" | "Int" => c.asInstanceOf[Rep[Int]] > v.asInstanceOf[String].toInt
          case "scala.Long" | "Long" => c.asInstanceOf[Rep[Long]] > v.asInstanceOf[String].toLong
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] > new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case "scala.Option[scala.Short]" => c.asInstanceOf[Rep[Option[Short]]] > v.asInstanceOf[String].toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] > v.asInstanceOf[String].toDouble
          case "scala.Option[scala.Int]" => c.asInstanceOf[Rep[Option[Int]]] > v.asInstanceOf[String].toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] > v.asInstanceOf[String].toLong
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] > new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case _ => {
              println("Type mapping for: " + col.`type` + " not found")
              None
          }
      }
   }
   def >=(col:Col, v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] >= v.asInstanceOf[String].toShort
          case "scala.Double" | "Double" => c.asInstanceOf[Rep[Double]] >= v.asInstanceOf[String].toDouble
          case "scala.Int" | "Int" => c.asInstanceOf[Rep[Int]] >= v.asInstanceOf[String].toInt
          case "scala.Long" | "Long" => c.asInstanceOf[Rep[Long]] >= v.asInstanceOf[String].toLong
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] >= new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case "scala.Option[scala.Short]" => c.asInstanceOf[Rep[Option[Short]]] >= v.asInstanceOf[String].toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] >= v.asInstanceOf[String].toDouble
          case "scala.Option[scala.Int]" => c.asInstanceOf[Rep[Option[Int]]] >= v.asInstanceOf[String].toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] >= v.asInstanceOf[String].toLong
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] >= new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case _ => {
              println("Type mapping for: " + col.`type` + " not found")
              None
          }
      }
   }

  def <(col:Col, v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] < v.asInstanceOf[String].toShort
          case "scala.Double" | "Double" => c.asInstanceOf[Rep[Double]] < v.asInstanceOf[String].toDouble
          case "scala.Int" | "Int" => c.asInstanceOf[Rep[Int]] < v.asInstanceOf[String].toInt
          case "scala.Long" | "Long" => c.asInstanceOf[Rep[Long]] < v.asInstanceOf[String].toLong
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] < new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case "scala.Option[scala.Short]" => c.asInstanceOf[Rep[Option[Short]]] < v.asInstanceOf[String].toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] < v.asInstanceOf[String].toDouble
          case "scala.Option[scala.Int]" => c.asInstanceOf[Rep[Option[Int]]] < v.asInstanceOf[String].toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] < v.asInstanceOf[String].toLong
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] < new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case _ => {
            println("Type mapping for: " + col.`type` + " not found")
            None
          }
      }
   }

  def <=(col:Col, v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    col.`type` match {
          case "scala.Short" | "Short" => c.asInstanceOf[Rep[Short]] <= v.asInstanceOf[String].toShort
          case "scala.Double" | "Double" => c.asInstanceOf[Rep[Double]] <= v.asInstanceOf[String].toDouble
          case "scala.Int" | "Int" => c.asInstanceOf[Rep[Int]] <= v.asInstanceOf[String].toInt
          case "scala.Long" | "Long" => c.asInstanceOf[Rep[Long]] <= v.asInstanceOf[String].toLong
          case "java.sql.Timestamp" => c.asInstanceOf[Rep[Timestamp]] <= new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case "scala.Option[scala.Short]" => c.asInstanceOf[Rep[Option[Short]]] <= v.asInstanceOf[String].toShort
          case "scala.Option[scala.Double]" => c.asInstanceOf[Rep[Option[Double]]] <= v.asInstanceOf[String].toDouble
          case "scala.Option[scala.Int]" => c.asInstanceOf[Rep[Option[Int]]] <= v.asInstanceOf[String].toInt
          case "scala.Option[scala.Long]" => c.asInstanceOf[Rep[Option[Long]]] <= v.asInstanceOf[String].toLong
          case "scala.Option[java.sql.Timestamp]" => c.asInstanceOf[Rep[Option[Timestamp]]] <= new Timestamp(timestampFormatter.parse((v.asInstanceOf[String])).getTime)
          case _ => {
            println("Type mapping for: " + col.`type` + " not found")
            None
          }
      }
   }

  def like(col:Col,v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val c:Rep[_] = col.rep

    println("Executing like on" + col.toString)

    col.`type` match {
          case "String" => c.asInstanceOf[Rep[String]] like v.asInstanceOf[String]
          case "scala.Option[String]" => c.asInstanceOf[Rep[Option[String]]] like v.asInstanceOf[String]
          case _ => {
            println("Type mapping for: " + col.`type` + " not found")
            None
          }
      }
   }

  def between(col:Col, v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

    val extremes = v.asInstanceOf[String].replace("to", "-").split('-')

    if (extremes.length == 2) {
      val lbound = >=(col, extremes(0))//.getOrElse(false)
      val ubound = <=(col, extremes(1))//.getOrElse(false)
      lbound && ubound //both need to be true
    } else {
      None
    }
  }

//  def in(col:Col, v:Any):Rep[Option[Boolean]] = ???
    def in(col:Col, v:Any):Rep[Option[Boolean]] = { //Returns Column[Boolean] or Column[Option[Boolean]]

      val elements = v.asInstanceOf[String].split(',').toSeq

      if (elements.size >0) {

        //      val reps: Seq[Rep[Boolean]] = elements.map(x => ==(col, x).getOrElse(false))
        val reps: Seq[Rep[Option[Boolean]]] = elements.map(x => this.==(col, x))

//        def mergeReps = (x:Rep[Option[Boolean]],y:Rep[Option[Boolean]]) => x || y  //need to be either true

        //      reps.fold[Rep[Option[Boolean]]](Some(true))((x,y) => x && y)

        reps.reduceLeft[Rep[Option[Boolean]]]((x,y) => x || y)

      } else{
        None
      }

    }
}