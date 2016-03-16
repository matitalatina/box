package ch.wsl.rest.logic

import ch.wsl.rest.service.Auth
import com.typesafe.config.{ConfigFactory, Config}
import slick.driver.PostgresDriver
import PostgresDriver.api._
import net.ceedubs.ficus.Ficus._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
  * Created by andreaminetti on 15/03/16.
  */
class PgInformationSchema(table:String, db:Database) {

  private val FOREIGNKEY = "FOREIGN KEY"
  private val PRIMARYKEY = "PRIMARY KEY"

  val pgColumns = TableQuery[PgColumns]
  val pgConstraints = TableQuery[PgConstraints]
  val pgConstraintsReference = TableQuery[PgConstraintReferences]
  val pgContraintsUsage = TableQuery[PgConstraintUsages]
  val pgKeyUsage = TableQuery[PgKeyUsages]

  case class ForeignKey(keys:Seq[String], referencingKeys:Seq[String], referencingTable:String, contraintName:String)

  val dbConf: Config = ConfigFactory.load().as[Config]("db")

  private val columnsQuery:Rep[Seq[PgColumns#TableElementType]] = pgColumns
    .filter(e => e.table_name === table && e.table_schema === dbConf.as[String]("schema"))
    .sortBy(_.ordinal_position)


  lazy val columns:Future[Seq[PgColumn]] = db.run{
    columnsQuery.result
  }

  private val pkQ:Rep[Seq[String]] = for{
    constraint <- pgConstraints if constraint.table_name === table && constraint.constraint_type === PRIMARYKEY
    usage <- pgContraintsUsage if usage.constraint_name === constraint.constraint_name && usage.table_name === table
  } yield usage.column_name

  def pk:Future[Seq[String]] = Auth.adminDB.run{ //needs admin right to access information_schema.constraint_column_usage
  val action = pkQ.result
    println(action.statements)
    action
  }

  private val fkQ1:Rep[Seq[(PgConstraintReferences#TableElementType,PgConstraints#TableElementType)]] = for{
    constraint <- pgConstraints if constraint.table_name === table && constraint.constraint_type === FOREIGNKEY
    constraintBind <- pgConstraintsReference if constraint.constraint_name === constraintBind.constraint_name
    referencingContraint <- pgConstraints if referencingContraint.constraint_name === constraintBind.referencing_constraint_name
  } yield (constraintBind,referencingContraint)

  private def fkQ2(c:PgConstraintReferences#TableElementType,ref:PgConstraints#TableElementType):Rep[Seq[(String,String)]] = for{
    usageRef <- pgContraintsUsage if usageRef.constraint_name === c.constraint_name && usageRef.table_name === ref.table_name
    usage <- pgKeyUsage if usage.constraint_name === c.constraint_name && usage.table_name === table
  } yield (usage.column_name,usageRef.column_name)



  lazy val fk:Future[Seq[ForeignKey]] =  {

    db.run(fkQ1.result).flatMap { references =>
      Future.sequence(references.map { case (c, ref) =>
        db.run(fkQ2(c,ref).result).map{ keys =>
          ForeignKey(keys.map(_._1),keys.map(_._2),ref.table_name, c.constraint_name)
        }
      })
    }

  }

  def findFk(field:String):Future[Option[ForeignKey]] = fk.map(_.find(_.keys.exists(_ == field)))

}