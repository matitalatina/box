package ch.wsl.box.model.shared

/**
 * Created by andreaminetti on 23/02/16.
 */
case class Table(headers:Vector[String], rows: Vector[Vector[(String,String)]], entity:JSONEntity)

object Table{
  def empty = Table(Vector(),Vector(),JSONEntity.empty)
}