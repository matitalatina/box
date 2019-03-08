package ch.wsl.box.rest.logic

import akka.stream.Materializer
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.utils.UserProfile
import scribe.Logging

import scala.concurrent.{ExecutionContext, Future}

case class BoxFormMetadataFactory(implicit up:UserProfile, mat:Materializer, ec:ExecutionContext) extends Logging with MetadataFactory {


  val main = JSONMetadata(
    objId = 1,
    name = "Interface builder",
    label = "Interface builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"form_id",false),
      JSONField(JSONFieldTypes.STRING,"name",false),
      JSONField(JSONFieldTypes.STRING,"description",true),
      JSONField(JSONFieldTypes.STRING,"layout",true, widget = Some(WidgetsNames.textarea),label = Some("")),
      JSONField(JSONFieldTypes.STRING,"entity",false),
      JSONField(JSONFieldTypes.STRING,"tabularFields",false),
      JSONField(JSONFieldTypes.STRING,"query",true),
      JSONField(JSONFieldTypes.STRING,"exportFields",true),
      JSONField(JSONFieldTypes.CHILD,"fields",true,child = Some(Child(2,"fields","form_id","form_id",None)))
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(Some("Base Info"),6,Seq("form_id","name","entity","query","description").map(Left(_))),
        LayoutBlock(Some("Table Info"),6,Seq("tabularFields","exportFields").map(Left(_))),
        LayoutBlock(Some("Layout"),12,Seq("layout").map(Left(_))),
        LayoutBlock(Some("Fields"),12,Seq("fields").map(Left(_))),
      )
    ),
    entity = "form",
    lang = "en",
    tabularFields = Seq("form_id","name","entity","description"),
    keys = Seq("form_id"),
    query = None,
    exportFields = Seq()
  )

  val field = JSONMetadata(
    objId = 2,
    name = "Field builder",
    label = "Field builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"field_id",false),
      JSONField(JSONFieldTypes.NUMBER,"form_id",false),
      JSONField(JSONFieldTypes.STRING,"name",false),
      JSONField(JSONFieldTypes.STRING,"widget",true),
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,12,Seq("field_id","form_id","name","widget").map(Left(_))),
      )
    ),
    entity = "field",
    lang = "en",
    tabularFields = Seq("field_id","form_id","name","widget"),
    keys = Seq("field_id"),
    query = None,
    exportFields = Seq()
  )



  val registry = Seq(main,field)

  override def list: Future[Seq[String]] = Future.successful(registry.map(_.name))

  override def of(name: String, lang: String): Future[JSONMetadata] = Future.successful(registry.find(_.name == name).get)

  override def of(id: Int, lang: String): Future[JSONMetadata] = Future.successful(registry.find(_.objId == id).get)

  override def children(form: JSONMetadata): Future[Seq[JSONMetadata]] = Future.successful{
    form match {
      case f if f == main => Seq(field)
      case _ => Seq()
    }
  }

}
