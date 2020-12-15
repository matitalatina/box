package ch.wsl.box.rest.metadata

import akka.stream.Materializer
import ch.wsl.box.information_schema.{PgColumn, PgInformationSchema}
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.utils.{Auth, BoxConfig, UserProfile}
import ch.wsl.box.shared.utils.JSONUtils
import com.typesafe.config.Config
import scribe.Logging
import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration._
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.jdbc.{FullDatabase, TypeMapping}
import ch.wsl.box.model.BoxFieldAccessRegistry
import ch.wsl.box.rest.runtime.{ColType, Registry}
import com.avsystem.commons.Try

import scala.concurrent.{Await, ExecutionContext, Future}


object EntityMetadataFactory extends Logging {


  val excludeFields:Seq[String] = Try{
    com.typesafe.config.ConfigFactory.load().as[Seq[String]]("db.generator.excludeFields")
  }.getOrElse(Seq())
  private var cacheTable = Map[(String, String, String, Int), Future[JSONMetadata]]()   //  (up.name, table, lang,lookupMaxRows)
  private var cacheKeys = Map[String, Future[Seq[String]]]()                            //  (table)

  def resetCache() = {
    cacheTable = Map()
    cacheKeys = Map()
  }

  def resetCacheForEntity(e:String) = {
    cacheTable = cacheTable.filterNot(c => CacheUtils.checkIfHasForeignKeys(e, c._2))
  }

  def lookupField(referencingTable:String,lang:String, firstNoPK:Option[String]):String = {

    val lookupLabelFields = BoxConfig.fksLookupLabels

    val default = lookupLabelFields.as[Option[String]]("default").getOrElse("name")

    val myDefaultTableLookupLabelField: String = default match {
      case "firstNoPKField" => firstNoPK.getOrElse("name")
      case JSONUtils.LANG => lang
      case _ => default
    }

    lookupLabelFields.as[Option[String]](referencingTable).getOrElse(myDefaultTableLookupLabelField)
  }


//  def lookup(table:String, column:String, lang:String)(implicit db:FullDatabase, ec:ExecutionContext):Future[Option[JSONFieldLookup]] = {
//
//
//    val schema = new PgInformationSchema(table, excludeFields)
//
//    for {
//      fkOpt <- schema.findFk(column)
//      firstNoPK <- fkOpt match {
//        case Some(f) => firstNoPKField(f.referencingTable)
//        case None => Future.successful(None)
//      }
//    } yield {
//      for{
//        fk <- fkOpt
//      } yield {
//        val text = lookupField(fk.referencingTable, lang, firstNoPK)
//        val model = fk.referencingTable
//        val value = fk.referencingKeys.head //todo verify for multiple key
//
//        JSONFieldLookup(model, JSONFieldMap(value, text, f), Seq())
//
//      }
//    }
//  }

  def of(table:String,lang:String, lookupMaxRows:Int = 100)(implicit up:UserProfile, mat:Materializer, ec:ExecutionContext,boxDatabase: FullDatabase):Future[JSONMetadata] = {

    implicit val db = up.db

    logger.warn("searching cache table for " + Seq(up.name, table, lang, lookupMaxRows).mkString)

    cacheTable.lift((up.name, table, lang,lookupMaxRows)) match {
      case Some(r) => r
      case None => {
        logger.info(s"Metadata table cache miss! cache key: ($table, $lang, $lookupMaxRows), cache: ${cacheTable}")

        val schema = new PgInformationSchema(table, excludeFields)

        //    println(schema.fk)

        var constraints = List[String]()

        def field2form(field: PgColumn): Future[JSONField] = {
          for {
            fk <- schema.findFk(field.column_name)
            firstNoPK <- fk match {
              case Some(f) => firstNoPKField(f.referencingTable)
              case None => Future.successful(None)
            }
            count <- fk match {
              case Some(fk) => db.run(Registry().actions(fk.referencingTable).count().map(_.count))
              case None => Future.successful(0)
            }
          } yield {
            fk match {
              case Some(fk) => {
                if (count <= lookupMaxRows) {
                  if (constraints.contains(fk.constraintName)) {
                    logger.info("error: " + fk.constraintName)
                    logger.info(field.column_name)
                    Future.successful(JSONField(field.jsonType, name = field.boxName, nullable = field.nullable))
                  } else {
                    constraints = fk.constraintName :: constraints //add fk constraint to contraint list


                    val text = lookupField(fk.referencingTable, lang, firstNoPK)

                    val model = fk.referencingTable
                    val value = fk.referencingKeys.head //todo verify for multiple keys


                    import ch.wsl.box.shared.utils.JSONUtils._
                    for {
                      lookupData <- db.run(Registry().actions(model).find())
                    } yield {
                      val options = lookupData.map { lookupRow =>
                        JSONLookup(lookupRow.get(value), lookupRow.get(text))
                      }

                      JSONField(
                        field.jsonType,
                        name = field.boxName,
                        nullable = field.nullable,
                        placeholder = Some(fk.referencingTable + " Lookup"),
                        //widget = Some(WidgetsNames.select),
                        lookup = Some(JSONFieldLookup(model, JSONFieldMap(value, text, field.boxName), options))
                      )
                    }

                  }
                } else { //no lookup from fk
                  Future.successful(JSONField(
                    field.jsonType,
                    name = field.boxName,
                    nullable = field.nullable,
                    widget = TypeMapping.defaultWidgetMapping(field.data_type)
                  ))
                }
              }
              case _ => Future.successful(JSONField(
                field.jsonType,
                name = field.boxName,
                nullable = field.nullable,
                widget = TypeMapping.defaultWidgetMapping(field.data_type)
              ))
            }
          }

        }.flatten

        val cacheKey = (up.name, table, lang, lookupMaxRows)

        val result = for {
          c <- schema.columns
          fields <- Future.sequence(c.map(field2form))
          keys <- EntityMetadataFactory.keysOf(table)
        } yield {
          val fieldList = fields.map(_.name)
          JSONMetadata(
            1,
            table,
            table,
            fields,
            Layout.fromFields(fields),
            table,
            lang,
            fieldList,
            fieldList,
            keys,
            None,
            fieldList,
            None,
            FormActionsMetadata.default
          )
        }
        if(BoxConfig.enableCache) {
          logger.warn("adding to cache table " + Seq(up.name, table, lang, lookupMaxRows).mkString)
          cacheTable = cacheTable ++ Map(cacheKey -> result)
        }
        result.onComplete{ x =>
          if(x.isFailure) {
            cacheTable = cacheTable.filterKeys(_ != cacheKey)
          }
        }
        result
      }
    }
  }

  def keysOf(table:String)(implicit ec:ExecutionContext, boxDb:FullDatabase):Future[Seq[String]] = {
    logger.info("Getting " + table + " keys")
    cacheKeys.lift((table)) match {
      case Some(r) => r
      case None => {
        logger.info(s"Metadata keys cache miss! cache key: ($table), cache: ${cacheKeys}")

        val result = new PgInformationSchema(table).pk.map { pk => //map to enter the future
          logger.info(pk.toString)
          pk.boxKeys
        }

        if(BoxConfig.enableCache) cacheKeys = cacheKeys ++ Map((table) -> result)
        result.onComplete{x =>
          if(x.isFailure) {
            cacheKeys = cacheKeys.filterKeys(_ != table)
          }
        }
        result
      }
    }
  }

  def firstNoPKField(table:String)(implicit db:FullDatabase, ec:ExecutionContext):Future[Option[String]] = {
    logger.info("Getting first field of " + table + " that is not PK")
    val schema = new PgInformationSchema(table,excludeFields)
    for {
      pks <- schema.pk.map(_.boxKeys) //todo: or boxKeys?
      c <- schema.columns
    } yield
    {
//      logger.info("PK's " + pks.mkString("-"))
//      logger.info("Columns " + c.map(_.column_name).mkString("-"))
//      logger.info("Columns " + c.map(_.column_name).diff(pks).mkString("-") + " that are not PK")
      c.map(_.column_name).diff(pks).headOption
    }
  }


  def fieldType(table:String,field:String):ColType = {
    val dbField = Registry().fields.field(table,field)
    if(dbField.name != "Unknown") {
      dbField
    } else {
      BoxFieldAccessRegistry.field(table,field)
    }
  }



  def isView(table:String)(implicit ec:ExecutionContext,boxDatabase: FullDatabase):Future[Boolean] =
    new PgInformationSchema(table).pgTable.map(_.isView)  //map to enter the future



}
