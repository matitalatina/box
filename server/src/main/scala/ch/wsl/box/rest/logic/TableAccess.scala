package ch.wsl.box.rest.logic

import ch.wsl.box.rest.utils.Auth
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext

object TableAccess {
  def write(table:String,schema:String,user:String)(implicit ec:ExecutionContext) = Auth.adminDB.run {
    sql"""SELECT 1
          FROM information_schema.role_table_grants
          WHERE table_name=$table and table_schema=$schema and grantee=$user and privilege_type='UPDATE'""".as[Int].headOption.map(_.isDefined)
  }
}
