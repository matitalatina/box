package ch.wsl.box.information_schema

import ch.wsl.box.jdbc.{FullDatabase, PostgresProfile, UserDatabase}
import ch.wsl.box.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by andreaminetti on 15/03/16.
  */
class PgInformationSchema(schema:String, table:String, excludeFields:Seq[String]=Seq())(implicit ec:ExecutionContext,db:UserDatabase) {


  private val FOREIGNKEY = "FOREIGN KEY"
  private val PRIMARYKEY = "PRIMARY KEY"

  private val pgTables = TableQuery[PgTables]
  private val pgColumns = TableQuery[PgColumns]
  private val pgConstraints = TableQuery[PgConstraints]
  private val pgConstraintsReference = TableQuery[PgConstraintReferences]
  private val pgContraintsUsage = TableQuery[PgConstraintUsages]
  private val pgKeyUsage = TableQuery[PgKeyUsages]

  case class PrimaryKey(keys: Seq[String], constraintName: String) {
    def boxKeys = keys
  }

  case class ForeignKey(keys: Seq[String], referencingKeys: Seq[String], referencingTable: String, constraintName: String) {
    def boxKeys = keys

    def boxReferencingKeys = referencingKeys
  }

  lazy val pgTable:Future[PgTable] = db.run{
    pgTables.filter(e => e.table_name === table && e.table_schema === schema).result.head
  }


  lazy val columns:Future[Seq[PgColumn]] = db.run{
    if (excludeFields.size==0)
      pgColumns
        .filter(e => e.table_name === table && e.table_schema === schema)
        .sortBy(_.ordinal_position).result
    else
      pgColumns
        .filter(e => e.table_name === table && e.table_schema === schema)
        .filterNot(_.column_name.inSet(excludeFields))
        .sortBy(_.ordinal_position).result
  }



  private val pkQ = for{
    constraint <- pgConstraints if constraint.table_name === table && constraint.constraint_type === PRIMARYKEY
    usage <- pgContraintsUsage if usage.constraint_name === constraint.constraint_name && usage.table_name === table
  } yield (usage.column_name, usage.constraint_name)

  val pk:Future[PrimaryKey] = db.run{ //needs admin right to access information_schema.constraint_column_usage
      pkQ.result
        .map(x => x.unzip)    //change seq of tuple into tuple of seqs
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



  lazy val fks:Future[Seq[ForeignKey]] =  {

    db.run(fkQ1.result).flatMap { references =>
      Future.sequence(references.map { case (c, ref) =>
        db.run(fkQ2(c,ref).result).map{ keys =>
          ForeignKey(keys.map(_._1),keys.map(_._2),ref.table_name, c.constraint_name)
        }
      })
    }

  }

  def findFk(field:String):Future[Option[ForeignKey]] = fks.map(_.find(_.keys.exists(_ == field)))

}