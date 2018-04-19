package ch.wsl.box.client

import ch.wsl.box.client.routes.Routes
import io.udash._

import scala.util.Try

sealed abstract class RoutingState(override val parentState: RoutingState) extends State {
  def url(implicit application: Application[RoutingState]): String = Try{application.matchState(this).value}.toOption match {
    case Some(v) => s"#$v"
    case None => ""
  }
}


case object LoginState extends RoutingState(RootState)

case object RootState extends RoutingState(null)

case object ErrorState extends RoutingState(RootState)

case object IndexState extends RoutingState(RootState)

case class EntitiesState(kind:String, currentEntity:String) extends RoutingState(RootState)

case class EntityTableState(kind:String, entity:String) extends RoutingState(EntitiesState(kind,entity))

case class EntityFormState(
                            kind:String,
                            entity:String,
                            id:Option[String]
                          ) extends RoutingState(EntitiesState(kind,entity))

case class MasterChildState(kind:String,
                            masterEntity:String,
                            childEntity:String
                           ) extends RoutingState(EntitiesState(kind,masterEntity))


case class ExportState(name:String) extends RoutingState(RootState)
