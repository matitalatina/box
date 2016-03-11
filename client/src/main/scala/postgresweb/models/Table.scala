package postgresweb.models

/**
 * Created by andreaminetti on 23/02/16.
 */
case class Table(headers:Vector[String],rows: Vector[Vector[String]], schema:JSONSchema)

object Table{
  def empty = Table(Vector(),Vector(),JSONSchema.empty)
}