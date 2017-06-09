package ch.wsl.box.client

import ch.wsl.box.client.routes.Routes
import io.udash._

sealed abstract class RoutingState(override val parentState: RoutingState) extends State {
  def url(implicit application: Application[RoutingState]): String = s"#${application.matchState(this).value}"
}


case object LoginState extends RoutingState(null)

case object RootState extends RoutingState(null)

case object ErrorState extends RoutingState(RootState)

case object IndexState extends RoutingState(RootState)

case class ModelsState(kind:String,model:String) extends RoutingState(RootState)

case class ModelTableState(kind:String,model:String) extends RoutingState(ModelsState(kind,model))


case class MasterChildState(kind:String,parentModel:String, childModel:String) extends RoutingState(ModelsState(kind,parentModel))

case class ModelFormState(
                           kind:String,
                           model:String,
                           id:Option[String]
                         ) extends RoutingState(ModelsState(kind,model))

case object FireState extends RoutingState(RootState)

case class FireFormState(id:Option[String]) extends RoutingState(RootState)


