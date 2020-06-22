package ch.wsl.box.jdbc

import slick.model.Column

object TypeMapping {

  def apply(model:Column): Option[String] = {
    model.options.find(_.isInstanceOf[slick.sql.SqlProfile.ColumnOption.SqlType]).flatMap {
      tpe =>
        tpe.asInstanceOf[slick.sql.SqlProfile.ColumnOption.SqlType].typeName match {
          case "hstore" => Option("Map[String, String]")
          case "varchar" => Option("String")                            // type 2003
          case "_text" | "text[]" | "_varchar" | "varchar[]" => Option("List[String]")
          case "_float8" | "float8[]" => Option("List[Double]")
          case "_float4" | "float4[]" => Option("List[Float]")
          case "_int8" | "int8[]" => Option("List[Long]")
          case "_int4" | "int4[]" => Option("List[Int]")
          case "_int2" | "int2[]" => Option("List[Short]")
          case "_decimal" | "decimal[]" | "_numeric" | "numeric[]"  => Option("List[scala.math.BigDecimal]")
          case _ => None
        }
    }.orElse {
      model.tpe match {
        case "java.sql.Date" => Some("java.time.LocalDate")
        case "java.sql.Time" => Some("java.time.LocalTime")
        case "java.sql.Timestamp" => Some("java.time.LocalDateTime")
        case _ =>
          None
      }
    }
  }


}
