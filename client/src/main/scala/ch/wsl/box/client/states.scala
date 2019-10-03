package ch.wsl.box.client

import ch.wsl.box.client.routes.Routes
import ch.wsl.box.model.shared.ExportDef
import io.udash._

import scala.scalajs.js.URIUtils
import scala.util.Try



sealed abstract class RoutingState(val parentState: Option[ContainerRoutingState]) extends State {
  type HierarchyRoot = RoutingState

  def url(implicit application: Application[RoutingState]): String =
    s"#${application.matchState(this).value}"
}

sealed abstract class ContainerRoutingState(parentState: Option[ContainerRoutingState]) extends RoutingState(parentState) with ContainerState
sealed abstract class FinalRoutingState(parentState: Option[ContainerRoutingState]) extends RoutingState(parentState) with FinalState


case object LoginState extends FinalRoutingState(Some(RootState))

case object RootState extends ContainerRoutingState(None)

case object ErrorState extends FinalRoutingState(Some(RootState))

case object AdminState extends FinalRoutingState(Some(RootState))

case object IndexState extends FinalRoutingState(Some(RootState))

case class EntitiesState(kind:String, currentEntity:String) extends ContainerRoutingState(Some(RootState))

case class EntityTableState(kind:String, entity:String) extends FinalRoutingState(Some(EntitiesState(kind,entity)))

case class EntityFormState(
                            kind:String,
                            entity:String,
                            write:String,
                            _id:Option[String]
                          ) extends FinalRoutingState(Some(EntitiesState(kind,entity))) {
  def id = {
    val t = _id.map(URIUtils.decodeURI)
    println(t)
    t
  }

  def writeable:Boolean = write == "true"
}

case class MasterChildState(kind:String,
                            masterEntity:String,
                            childEntity:String
                           ) extends FinalRoutingState(Some(EntitiesState(kind,masterEntity)))


object DataKind{
  final val EXPORT = "export"
  final val FUNCTION = "function"
  final val PDF = "pdf"
}

case class DataListState(kind:String,currentExport:String) extends ContainerRoutingState(Some(RootState))
case class DataState(kind:String,export:String) extends FinalRoutingState(Some(DataListState(kind,export)))
