package ch.wsl.box.rest.logic

import ch.wsl.box.rest.utils.Auth
import slick.driver.PostgresDriver
import PostgresDriver.api._
import net.ceedubs.ficus.Ficus._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import StringHelper._
import slick.lifted.ShapedValue

/**
  * Created by andreaminetti on 15/03/16.
  */
class PgInformationSchema(table:String, db:Database, pgSchema:String="public") {

  private val FOREIGNKEY = "FOREIGN KEY"
  private val PRIMARYKEY = "PRIMARY KEY"

  val pgColumns = TableQuery[PgColumns]
  val pgConstraints = TableQuery[PgConstraints]
  val pgConstraintsReference = TableQuery[PgConstraintReferences]
  val pgContraintsUsage = TableQuery[PgConstraintUsages]
  val pgKeyUsage = TableQuery[PgKeyUsages]

  case class PrimaryKey(keys:Seq[String], constraintName:String){
    def boxKeys = keys.map(_.slickfy)
  }
  case class ForeignKey(keys:Seq[String], referencingKeys:Seq[String], referencingTable:String, constraintName:String){
    def boxKeys = keys.map(_.slickfy)
    def boxReferencingKeys = referencingKeys.map(_.slickfy)
  }


  private val columnsQuery = pgColumns
    .filter(e => e.table_name === table && e.table_schema === pgSchema)
    .sortBy(_.ordinal_position)


  lazy val columns:Future[Seq[PgColumn]] = db.run{
    columnsQuery.result
  }

  val pkQ = for{
    constraint <- pgConstraints if constraint.table_name === table && constraint.constraint_type === PRIMARYKEY
    usage <- pgContraintsUsage if usage.constraint_name === constraint.constraint_name && usage.table_name === table
  } yield (usage.column_name, usage.constraint_name)

  def pk:Future[PrimaryKey] = Auth.adminDB.run{ //needs admin right to access information_schema.constraint_column_usage
      pkQ.result
        .map(x => x.unzip)    //change seq of tuple into tuple od seqs
        .map(x => PrimaryKey(x._1, x._2.headOption.getOrElse("")))   //as constraint_name take only first element (should be the same)
  }

  private val fkQ1 = for{
    constraint <- pgConstraints if constraint.table_name === table && constraint.constraint_type === FOREIGNKEY
    constraintBind <- pgConstraintsReference if constraint.constraint_name === constraintBind.constraint_name
    referencingConstraint <- pgConstraints if referencingConstraint.constraint_name === constraintBind.referencing_constraint_name
  } yield (constraintBind,referencingConstraint)

  private def fkQ2(c:PgConstraintReferences#TableElementType,ref:PgConstraints#TableElementType) = for{
    usageRef <- pgContraintsUsage if usageRef.constraint_name === c.constraint_name && usageRef.table_name === ref.table_name
    usage <- pgKeyUsage if usage.constraint_name === c.constraint_name && usage.table_name === table
  } yield (usage.column_name,usageRef.column_name)



  lazy val fk:Future[Seq[ForeignKey]] =  {

    Auth.adminDB.run(fkQ1.result).flatMap { references =>
      Future.sequence(references.map { case (c, ref) =>
        Auth.adminDB.run(fkQ2(c,ref).result).map{ keys =>
          ForeignKey(keys.map(_._1),keys.map(_._2),ref.table_name, c.constraint_name)
        }
      })
    }

  }

  def findFk(field:String):Future[Option[ForeignKey]] = fk.map(_.find(_.keys.exists(_ == field)))

}