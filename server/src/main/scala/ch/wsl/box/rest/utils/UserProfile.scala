package ch.wsl.box.rest.utils

import ch.wsl.box.model.boxentities.User
//import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.jdbc.PostgresProfile.plainAPI._
import com.github.tminglei.slickpg.utils.PlainSQLUtils._
import slick.jdbc.GetResult

import scala.concurrent.{ExecutionContext, Future}

case class UserProfile(name: String, db: Database, boxDb:Database) {

  def check(implicit ec:ExecutionContext): Future[Boolean] = db.run{
    sql"""select 1""".as[Int]
  }.map{ _ =>
    true
  }.recover{case _ => false}

  def accessLevel(implicit ec:ExecutionContext):Future[Int] = Auth.boxDB.run{
    User.table.filter(_.username === name).result
  }.map(_.headOption.map(_.access_level_id).getOrElse(-1))

  def boxUserProfile = UserProfile(name, boxDb, boxDb)   //todo : do it less ugly


  def memberOf(implicit ec:ExecutionContext) = Auth.adminDB.run{              //todo: depends on v_roles, hasrole and hasrolein >> make cleaner
    sql"""select memberOf from box.v_roles where lower(rolname)=lower(current_user)""".as[List[String]](GetResult{r=> r.<<[Seq[String]].toList})

  }.map{ _.head
  }

  def hasRole(role:String)(implicit ec:ExecutionContext) = Auth.adminDB.run{
    sql"""select box.hasrole($role)""".as[Boolean]
  }.map{ _.head
  }.recover{case _ => false}

  def hasRoleIn(roles:List[String])(implicit ec:ExecutionContext) = Auth.adminDB.run{
    sql"""select box.hasrolein(ARRAY[${roles.map("'"+_+"'").mkString(",")}])""".as[Boolean]
  }.map{ _.head
  }.recover{case _ => false}



}
