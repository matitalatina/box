package ch.wsl.box.client

import io.udash._

sealed abstract class RoutingState(val parentState: RoutingState) extends State {
  def url(implicit application: Application[RoutingState]): String = s"#${application.matchState(this).value}"
}

case object RootState extends RoutingState(null)

case object ErrorState extends RoutingState(RootState)

case object IndexState extends RoutingState(RootState)

case class ModelsState(model:String) extends RoutingState(RootState)

case class ModelTableState(model:String) extends RoutingState(ModelsState(model))
case class ModelFormState(model:String, id:Option[String]) extends RoutingState(ModelsState(model))


