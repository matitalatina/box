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

sealed abstract class ContainerRoutingState(parentState: Option[ContainerRoutingState]) extends RoutingState(parentState)
sealed abstract class FinalRoutingState(parentState: Option[ContainerRoutingState]) extends RoutingState(parentState)


sealed abstract class LoginStateAbstract(parentState: Option[ContainerRoutingState]) extends FinalRoutingState(parentState)
case class LoginState(url:String) extends LoginStateAbstract(Some(RootState))
case object LogoutState extends LoginStateAbstract(Some(RootState))
case class LoginState1param(url:String,p1:String) extends LoginStateAbstract(Some(RootState))
case class LoginState2params(url:String,p1:String,p2:String) extends LoginStateAbstract(Some(RootState))
case class LoginState3params(url:String,p1:String,p2:String,p3:String) extends LoginStateAbstract(Some(RootState))
case class LoginState4params(url:String,p1:String,p2:String,p3:String,p4:String) extends LoginStateAbstract(Some(RootState))

case object RootState extends ContainerRoutingState(None)

case object ErrorState extends FinalRoutingState(Some(RootState))

case object AdminState extends FinalRoutingState(Some(RootState))
case object AdminConfState extends FinalRoutingState(Some(RootState))
case object AdminUiConfState extends FinalRoutingState(Some(RootState))
case object AdminBoxDefinitionState extends FinalRoutingState(Some(RootState))

case object IndexState extends FinalRoutingState(Some(RootState))

case class EntitiesState(kind:String, currentEntity:String) extends ContainerRoutingState(Some(RootState))

case class EntityTableState(kind:String, entity:String) extends FinalRoutingState(Some(EntitiesState(kind,entity)))

abstract class FormState(
                          val kind:String,
                          val entity:String,
                          val write:String,
                          _id:Option[String],
                          val public:Boolean
                        ) extends FinalRoutingState(Some(EntitiesState(kind,entity))) {
  def id:Option[String] = _id
  def writeable:Boolean = write == "true"
}

case class EntityFormState(
                            override val kind:String,
                            override val entity:String,
                            override val write:String,
                            _id:Option[String],
                            override val public:Boolean
                          ) extends FormState(kind, entity, write, _id, public) {
  override def id = {
    val t = _id.map(URIUtils.decodeURI)
    t
  }


}

case class FormPageState(
                          override val kind:String,
                          override val entity:String,
                          override val write:String,
                          override val public:Boolean
                          ) extends FormState(kind,entity,write,Some("static::page"),public)

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
