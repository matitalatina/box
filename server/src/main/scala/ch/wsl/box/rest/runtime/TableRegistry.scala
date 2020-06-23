package ch.wsl.box.rest.runtime

import akka.stream.Materializer
import ch.wsl.box.jdbc.PostgresProfile.api._
import ch.wsl.box.model.boxentities.BoxPublicEntities
import ch.wsl.box.model.shared.{JSONSort, Sort}
import ch.wsl.box.rest.logic.{Col}
import ch.wsl.box.rest.metadata.EntityMetadataFactory
import ch.wsl.box.rest.utils.UserProfile
import scribe.Logging
import slick.lifted.ColumnOrdered

import scala.reflect.runtime.universe._
import scala.concurrent.{ExecutionContext, Future}

trait TableRegistryEntry extends Logging {

  type MT

  import ch.wsl.box.rest.logic.EnhancedTable._


  def sort(sort: JSONSort, lang: String, query: Query[Table[MT], MT, Seq])(implicit ec: ExecutionContext, up: UserProfile, mat: Materializer): Future[Query[Table[MT], MT, Seq]] = {
    EntityMetadataFactory.of(name, lang).map {
      _.fields.find(_.name == sort.column).flatMap(_.lookup) match {
        case None => query.sortBy { x =>
          sort.order match {
            case Sort.ASC => ColumnOrdered(x.col(sort.column).rep, new slick.ast.Ordering)
            case Sort.DESC => ColumnOrdered(x.col(sort.column).rep, new slick.ast.Ordering(direction = slick.ast.Ordering.Desc))
          }
        }
        case Some(lookup) => {

          BoxPublicEntities.table.joinLeft(BoxPublicEntities.table)

          val externalTable = Registry().tables.table(lookup.lookupEntity)
          query
            .joinLeft(externalTable.tableQuery)
            .on { case (m, c) => EnTable(c).col(lookup.map.valueProperty).rep.asInstanceOf[Rep[Int]] === m.col(sort.column).asInstanceOf[Rep[Int]] }
            .map{ case (m,j) => m }

        }
      }
    }

  }




  def name:String

  def tableQuery: TableQuery[Table[MT]]
}

trait TableRegistry {
  def table(name:String):TableRegistryEntry
}
