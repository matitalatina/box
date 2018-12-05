package ch.wsl.box.codegen

import com.typesafe.config.Config
import slick.model.Model
import slick.ast.ColumnOption



trait MyOutputHelper extends slick.codegen.OutputHelpers {
  override def packageCode(profile: String, pkg: String, container: String, parentType: Option[String]) : String= {
    s"""
       |package ${pkg}
       |// AUTO-GENERATED Slick data model
       |/** Stand-alone Slick data model for immediate use */
       |
       |  import ch.wsl.box.rest.jdbc.PostgresProfile.api._
       |  import slick.model.ForeignKeyAction
       |  import slick.collection.heterogeneous._
       |  import slick.collection.heterogeneous.syntax._
       |
       |object $container {
       |
       |
       |      val profile = ch.wsl.box.rest.jdbc.PostgresProfile
       |
       |      import profile._
       |
       |          ${indent(code)}
       |}
     """.stripMargin.trim
  }
}

//exteded code generator (add route and registry generation)
case class EntitiesGenerator(model:Model, conf:Config) extends slick.codegen.SourceCodeGenerator(model) with BoxSourceCodeGenerator with MyOutputHelper {




  // override table generator
  override def Table = new Table(_){

    //disable plain override mapping, with hlist produces compile time problem with non-primitives types
    override def PlainSqlMapper = new PlainSqlMapperDef {
      override def code = ""
    }

    // disable entity class generation and mapping
    override def EntityType = new EntityType{



      override def code = {
        val args = columns.map { c =>
          c.default.map(v =>
            s"${c.name}: ${c.exposedType} = $v"
          ).getOrElse(
            s"${c.name}: ${c.exposedType}"
          )
        }.mkString(", ")

        val prns = (parents.take(1).map(" extends "+_) ++ parents.drop(1).map(" with "+_)).mkString("")
        val result = s"""case class $name($args)$prns"""

        if(model.columns.size <= 22) {
          result
        } else {
          result + s"""
    object ${TableClass.elementType}{

      type ${TableClass.elementType}HList = ${columns.map(_.exposedType).mkString(" :: ")} :: HNil

      def factoryHList(hlist:${TableClass.elementType}HList):${TableClass.elementType} = {
        val x = hlist.toList
        ${TableClass.elementType}("""+columns.zipWithIndex.map(x => "x("+x._2+").asInstanceOf["+x._1.exposedType+"]").mkString(",")+s""");
      }

      def toHList(e:${TableClass.elementType}):Option[${TableClass.elementType}HList] = {
        Option(( """+columns.map(c => "e."+ c.name + " :: ").mkString("")+s""" HNil))
      }
    }
                 """
        }

      }
    }



    override def factory = if(model.columns.size <= 22 ) {
      super.factory
    } else {

      s"${TableClass.elementType}.factoryHList"

    }

    override def extractor = if(model.columns.size <= 22 ) {
      super.extractor
    } else {
      s"${TableClass.elementType}.toHList"
    }

    override def mappingEnabled = true;

    override def TableClass = new TableClassDef {
      override def optionEnabled = columns.size <= 22 && mappingEnabled && columns.exists(c => !c.model.nullable)
    }

    def tableModel = model

    override def ForeignKey = new ForeignKeyDef(_) {
      //override def code: String = "" // dont generate foreign keys
    }

    override def Column = new Column(_){
      // customize Scala column names
      override def rawName = model.name

      override def rawType: String = {
        model.options.find(_.isInstanceOf[slick.sql.SqlProfile.ColumnOption.SqlType]).flatMap {
          tpe =>
            tpe.asInstanceOf[slick.sql.SqlProfile.ColumnOption.SqlType].typeName match {
              case "hstore" => Option("Map[String, String]")
              case "_text" | "text[]" | "_varchar" | "varchar[]" => Option("List[String]")
              case "_varchar" => Option("List[String]") // type 2003
              case "geometry" => Option("com.vividsolutions.jts.geom.Geometry")
              case "_int8" | "int8[]" => Option("List[Long]")
              case "_int4" | "int4[]" => Option("List[Int]")
              case "_int2" | "int2[]" => Option("List[Short]")
              case _ => None
            }
        }.getOrElse {
          model.tpe match {
            case "java.sql.Date" => "java.time.LocalDate"
            case "java.sql.Time" => "java.time.LocalTime"
            case "java.sql.Timestamp" => "java.time.LocalDateTime"
            case _ =>
              super.rawType

          }
        }
      }


      private def primaryKey = {
        val singleKey = model.options.contains(ColumnOption.PrimaryKey)
        val multipleKey = tableModel.primaryKey.exists(_.columns.exists(_.name == model.name))
        singleKey || multipleKey
      }


      def completeName = s"${model.table.table}.${model.name}"

      val dbKeysExceptions = conf.getStringList("generator.keys.db")
      val appKeysExceptions = conf.getStringList("generator.keys.app")
      val keyStrategy = conf.getString("generator.keys.default.strategy")

      private val managed:Boolean = {
        val result = keyStrategy match {
          case "db" if primaryKey => !dbKeysExceptions.contains(completeName)
          case "app" if primaryKey => dbKeysExceptions.contains(completeName)
          case _ => false
        }
        result
      }

      override def asOption: Boolean =  managed match {
        case true => true
        case false => super.asOption
      }

      override def options: Iterable[String] = managed match {
        case false => super.options
        case true => {super.options.toSeq ++ Seq("O.AutoInc")}.distinct
      }


    }


  }

}