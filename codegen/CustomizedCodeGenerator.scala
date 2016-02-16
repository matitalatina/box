
package ch.wsl.codegen

import slick.model.Model
import slick.driver.PostgresDriver


import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *  This customizes the Slick code generator. We only do simple name mappings.
 *  For a more advanced example see https://github.com/cvogt/slick-presentation/tree/scala-exchange-2013
 */
object CustomizedCodeGenerator {
  def main(args: Array[String]):Unit = {


    println("Generating " + args(0))


    val db = PostgresDriver.api.Database.forURL("jdbc:postgresql:incendi", driver = "org.postgresql.Driver", user = "andreaminetti", password = "")

//    val defaultTables = db.run{PostgresDriver.defaultTables}
//    val dt = Await.result(defaultTables, 100 seconds)
//
//    println(dt)


    val model = Await.result(db.run{
      PostgresDriver.createModel(Some(PostgresDriver.defaultTables))
    }, 200 seconds)



    trait MyOutputHelper extends slick.codegen.OutputHelpers {
      override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]) : String= {
        s"""
package ${pkg}
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */

  import slick.driver.PostgresDriver.api._
  import slick.model.ForeignKeyAction
  import slick.collection.heterogeneous._
  import slick.collection.heterogeneous.syntax._

package object tables {


      val profile = slick.driver.PostgresDriver

          ${indent(code)}
}
      """.trim()
      }
    }

    val codegen = new slick.codegen.SourceCodeGenerator(model) with MyOutputHelper {




      // override table generator
      override def Table = new Table(_){
        // disable entity class generation and mapping
        override def EntityType = new EntityType{
          override def code = {
            val args = columns.map(c=>
              c.default.map( v =>
                s"${c.name}: ${c.exposedType} = $v"
              ).getOrElse(
                  s"${c.name}: ${c.exposedType}"
                )
            ).mkString(", ")

              val prns = (parents.take(1).map(" extends "+_) ++ parents.drop(1).map(" with "+_)).mkString("")
              val result = s"""case class $name($args)$prns"""

              if(model.columns.size < 22) {
                result
              } else {
                result + s"""
    object ${TableClass.elementType}{
      def factoryHList(hlist:HList) = {
        val x = hlist.toList
        ${TableClass.elementType}("""+columns.zipWithIndex.map(x => "x("+x._2+").asInstanceOf["+x._1.actualType+"]").mkString(",")+s""");
      }

      def toHList(e:${TableClass.elementType}) = {
        Option(( """+columns.map(c => "e."+ c.rawName + " :: ").mkString("")+s""" HNil))
      }
    }
                 """
              }

          }
        }



        override def factory = if(model.columns.size < 22 ) {
          super.factory
        } else {

          s"${TableClass.elementType}.factoryHList"

        }

        override def extractor = if(model.columns.size < 22 ) {
          super.extractor
        } else {
          s"${TableClass.elementType}.toHList"
        }

        override def mappingEnabled = true;

        override def TableClass = new TableClassDef {
          override def optionEnabled = columns.size < 22 && mappingEnabled && columns.exists(c => !c.model.nullable)
        }


      }

    }


    println("codegen created")
    println(codegen)

    codegen.writeToFile(
      "slick.driver.PostgresDriver",
      args(0),
      "ch.wsl.model",
      "Tables",
      "Tables.scala"
    )


      println("Exit")


  }





//  val db = PostgresDriver.api.Database.forURL(url, driver = jdbcDriver, user = user, password = password)
//  // filter out desired tables
//
//  val codegen = db.run{
//    PostgresDriver.defaultTables.flatMap( PostgresDriver.createModelBuilder(_,false).buildModel )
//  }.map{ model =>
//    new slick.codegen.SourceCodeGenerator(model){
//      // customize Scala entity name (case class, etc.)
//      override def entityName = dbTableName => dbTableName match {
//        case "COFFEES" => "Coffee"
//        case "SUPPLIERS" => "Supplier"
//        case "COF_INVENTORY" => "CoffeeInventoryItem"
//        case _ => super.entityName(dbTableName)
//      }
//      // customize Scala table name (table class, table values, ...)
//      override def tableName = dbTableName => dbTableName match {
//        case "COF_INVENTORY" => "CoffeeInventory"
//        case _ => super.tableName(dbTableName)
//      }
//      // override generator responsible for tables
//      override def Table = new Table(_){
//        table =>
//        // customize table value (TableQuery) name (uses tableName as a basis)
//        override def TableValue = new TableValue{
//          override def rawName = super.rawName.uncapitalize
//        }
//        // override generator responsible for columns
//        override def Column = new Column(_){
//          // customize Scala column names
//          override def rawName = (table.model.name.table,this.model.name) match {
//            case ("COFFEES","COF_NAME") => "name"
//            case ("COFFEES","SUP_ID") => "supplierId"
//            case ("SUPPLIERS","SUP_ID") => "id"
//            case ("SUPPLIERS","SUP_NAME") => "name"
//            case ("COF_INVENTORY","QUAN") => "quantity"
//            case ("COF_INVENTORY","COF_NAME") => "coffeeName"
//            case _ => super.rawName
//          }
//        }
//      }
//    }
//  }

}

//  val model = db.withSession { implicit session =>
//
//    if (!PostgresDriver.getTables.list.exists(_.name.name == "sys_form")) {
//      val sysForms = TableQuery[SysForm]
//      sysForms.ddl.create
//    }
//
//    val tables = PostgresDriver.getTables.list.filter(t => t.name.schema.exists(_ == "public"))
//
//
//
//
//    createModel(tables, PostgresDriver)
//  }
//
//
//  trait MyOutputHelpers extends scala.slick.codegen.OutputHelpers{
//    /**
//   * Generates code providing the data model as trait and object in a Scala package
//   * @group Basic customization overrides
//   * @param profile Slick profile that is imported in the generated package (e.g. slick.driver.H2Driver)
//   * @param pkg Scala package the generated code is placed in
//   * @param container The name of a trait and an object the generated code will be placed in within the specified package.
//   */
//  override def packageCode(profile: String, pkg: String, container:String="Tables") : String = {
//      s"""
//package ${pkg}
//// AUTO-GENERATED Slick data model
///** Stand-alone Slick data model for immediate use */
//
//  import scala.slick.driver.PostgresDriver.simple._
//
//
//  /**Base class for the table entity object, that contains some handy methods on models
//  *like:
//  * _exists_
//  * _find_
//  * _get_
//  * _save_ (which inserts or updates)
//  *
//  */
//abstract class TableUtils[T <: Table[M], M](val tq:TableQuery[T]) {
//
//
//  def columns:Map[String,T => scala.slick.driver.PostgresDriver.simple.Column[_]]
//
//  def filter(field:String,op:(scala.slick.driver.PostgresDriver.simple.Column[_],Any) => scala.slick.driver.PostgresDriver.simple.Column[Option[Boolean]],v:Any)(x:T) = {
//          val c = columns(field)(x)
//          op(c,v)
//  }
//
//
//  /** Returns the table length (number of rows) */
//  def count(implicit s:Session) = tq.length.run
//
//  /** Calculates the number of pages according to a page size*/
//  def pages(size:Int)(implicit s:Session) = (count / size.toDouble).ceil.toInt
//
//}
//
//trait PrimaryKey[T <: Table[M], M] extends TableUtils[T,M] {
//  /** Checks if the specified model exists in the table */
//  def exists(model: M)(implicit s:Session):Boolean = find(model).exists.run
//
//  /** Returns the query result (like a recordset), according to a specified model */
//  def find(model: M):Query[T,M, Seq]
//
//  /** Returns the first model as an Option, according to a specified model*/
//  def get(model: M)(implicit s:Session):Option[M] = this.find(model).firstOption
//
//}
//
//trait WriteTable[T <: Table[M], M] extends PrimaryKey[T,M] {
//
//   /**Saves a model to the table.
//   *
//   * It first check if it already exists in the table, and accordingly uses
//   * an insert or an update statement.
//   *
//   * This method is synchronized to avoid concurrent savings with unexpected results
//   */
//
//  def save(model:M)(implicit s:Session):Int = this.synchronized {     //returns the number of rows affected
//    val row = this.find(model)
//
//    row.firstOption match {
//        case None => tq.insert(model)
//        case Some(x) => if (x != model) row.update(model) else 0
//    }
//  }
//
//}
//
//  ${indent(code)}
//
//      """.trim()
//    }
//  }
//
//  val codegen = new slick.codegen.SourceCodeGenerator(model) with MyOutputHelpers{
//
//
//  override def code = {
//    "import scala.slick.model.ForeignKeyAction\n" +
//    ( if(tables.exists(_.hlistEnabled)){
//        "import scala.slick.collection.heterogenous._\n"+
//        "import scala.slick.collection.heterogenous.syntax._\n"
//      } else ""
//    ) +
//    ( if(tables.exists(_.PlainSqlMapper.enabled)){
//        "// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.\n"+
//        "import scala.slick.jdbc.{GetResult => GR}\n"
//      } else ""
//    ) +
//    "\n\n" +
//    tables.map(_.code.mkString("\n")).mkString("\n\n")
//  }
//
//  override type Table = TableDef
//  def Table = new Table(_){
//
//
//    val pkMulti = model.primaryKey.toList.flatMap(_.columns)
//
//    val pks = if(pkMulti.size > 0) {
//      pkMulti
//    } else {
//      this.model.columns.filter(_.options.exists{ e=> e.toString match {
//        case "PrimaryKey" => true
//        case _ => false
//      }})
//    }
//
//    // Using defs instead of (caching) lazy vals here to provide consitent interface to the user.
//    // Performance should really not be critical in the code generator. Models shouldn't be huge.
//    // Also lazy vals don't inherit docs from defs
//    type EntityType     =     MyEntityTypeDef
//    def  EntityType     = new EntityType{}
//    type PlainSqlMapper =     PlainSqlMapperDef
//    def  PlainSqlMapper = new PlainSqlMapper{
//      override def code = ""
//    }
//    type TableClass     =     TableClassDef
//    def  TableClass     = new TableClass{
//      override def option = if(model.columns.size < 22 ) {
//          super.option
//        } else {
//          ""
//        }
//    }
//    type TableValue     =     TableValueDef
//    def  TableValue     = new TableValue{
//      override def code = if(pks.size == 0) s"""
//object $name extends TableUtils[$name, ${name}Row] (TableQuery[$name]) {
//
//    def columns = Map(
//      """ + columnsByName.map(_._1).map(c =>  "\""+ c + "\" -> { (x:"+ name + ") => x." + c + " }" ).mkString(",\n") + """
//    )
//
//}
//        """ else s"""
//object $name extends TableUtils[$name, ${name}Row] (TableQuery[$name]) with WriteTable[$name, ${name}Row] {
//
//    def find(model:${name}Row) = tq.filter(table => """+ pks.map(x=> " table."+x.name+" === model."+x.name+" ").mkString("&&") + s""")
//
//    def columns = Map(
//      """ + columnsByName.map(_._1).map(c =>  "\""+ c + "\" -> { (x:"+ name + ") => x." + c + " }" ).mkString(",\n") + """
//    )
//
//}
//      """;
//    }
//    type PrimaryKey     =     PrimaryKeyDef
//    def  PrimaryKey     = new PrimaryKey(_)
//    type ForeignKey     =     ForeignKeyDef
//    def  ForeignKey     = new ForeignKey(_) {
//      override def code = "";
//    }
//    type Index          =     IndexDef
//    def  Index          = new Index(_)
//    type Column         =     ColumnDef
//    def  Column         = new Column(_){
//          // customize Scala column names
//          override def rawName = this.model.name
//    }
//
//
//
//    override def factory = if(model.columns.size < 22 ) {
//      super.factory
//    } else {
//
//      s"${TableClass.elementType}.factoryHList"
//
//    }
//
//    override def extractor = if(model.columns.size < 22 ) {
//      super.extractor
//    } else {
//      s"${TableClass.elementType}.toHList"
//    }
//
//    override def mappingEnabled = true;
//
//
//    trait MyEntityTypeDef extends super.EntityTypeDef{
//      override def code = {
//        val args = columns.map(c=>
//          c.default.map( v =>
//            s"${c.name}: ${c.exposedType} = $v"
//          ).getOrElse(
//            s"${c.name}: ${c.exposedType}"
//          )
//        ).mkString(", ")
//        if(classEnabled){
//          val prns = (parents.take(1).map(" extends "+_) ++ parents.drop(1).map(" with "+_)).mkString("")
//          val result = s"""case class $name($args)$prns"""
//
//          if(model.columns.size < 22) {
//            result
//          } else {
//            result + s"""
//    object ${TableClass.elementType}{
//      def factoryHList(hlist:HList) = {
//        val x = hlist.toList
//        ${TableClass.elementType}("""+columns.zipWithIndex.map(x => "x("+x._2+").asInstanceOf["+x._1.actualType+"]").mkString(",")+s""");
//      }
//
//      def toHList(e:${TableClass.elementType}) = {
//        Option(( """+columns.map(c => "e."+ c.rawName + " :: ").mkString("")+s""" HNil))
//      }
//    }
//                 """
//              }
//            } else {
//              s"""
//    type $name = $types
//    /** Constructor for $name providing default values if available in the database schema. */
//    def $name($args): $name = {
//      ${compoundValue(columns.map(_.name))}
//    }
//              """.trim
//            }
//          }
//        }
//
//
//    }
//
//
//  }
//
//
//}