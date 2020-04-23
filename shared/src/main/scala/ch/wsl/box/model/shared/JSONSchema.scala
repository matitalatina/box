package ch.wsl.box.model.shared

/**
 * Created by andreaminetti on 16/03/16.
 */

case class JSONSchema(
                       `type`:String,
                       title:Option[String] = None,           //table name
                       properties: Map[String,JSONSchema] = Map(),
                       required: Option[Seq[String]] = None,  //columns that are required
                       readonly: Option[Boolean] = None,      // if all columns are readonly
                       enum: Option[Seq[String]] = None,
                       order: Option[Int] = None
                     )

