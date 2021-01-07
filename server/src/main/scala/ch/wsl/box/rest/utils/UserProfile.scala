package ch.wsl.box.rest.utils

import ch.wsl.box.jdbc.UserDatabase
import ch.wsl.box.model.boxentities.BoxUser
import slick.basic.DatabasePublisher
import slick.dbio
import slick.dbio.{DBIOAction, NoStream}
import slick.sql.SqlAction
import ch.wsl.box.jdbc.PostgresProfile.api._
import com.github.tminglei.slickpg.utils.PlainSQLUtils._
import slick.jdbc.GetResult


import scala.concurrent.{ExecutionContext, Future}

case class UserProfile(name: String) {


  def db(implicit executionContext: ExecutionContext) = new UserDatabase {

    //cannot interpolate directly
    val setRole: SqlAction[Int, NoStream, Effect] = sqlu"SET ROLE placeholder".overrideStatements(Seq(s"SET ROLE $name"))
    val resetRole = sqlu"RESET ROLE"

    override def stream[T](a: DBIOAction[Seq[T], Streaming[T], Nothing]) = {

      Auth.adminDB.stream[T](
        setRole.andThen[Seq[T],Streaming[T],Nothing](a).andFinally(resetRole)
      )


    }

    override def run[R](a: DBIOAction[R, NoStream, Nothing]) = {
      Auth.adminDB.run {
        setRole.andThen[R,NoStream,Nothing](a).andFinally(resetRole)
      }
    }
  }

  def accessLevel(implicit ec:ExecutionContext):Future[Int] = Auth.adminDB.run{
    BoxUser.BoxUserTable.filter(_.username === name).result
  }.map(_.headOption.map(_.access_level_id).getOrElse(-1))


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
