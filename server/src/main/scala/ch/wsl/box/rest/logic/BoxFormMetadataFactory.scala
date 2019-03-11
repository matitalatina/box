package ch.wsl.box.rest.logic

import akka.stream.Materializer
import ch.wsl.box.model.boxentities.Form
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.utils.{Auth, UserProfile}
import scribe.Logging
import io.circe._
import io.circe.syntax._
import ch.wsl.box.model.Entities.profile.api._
import ch.wsl.box.rest.routes.Table

import scala.concurrent.{ExecutionContext, Future}

case class BoxFormMetadataFactory(implicit up:UserProfile, mat:Materializer, ec:ExecutionContext) extends Logging with MetadataFactory {


  final val FORM = 1
  final val FIELD = 2

  def main(tables:Seq[String]) = JSONMetadata(
    objId = FORM,
    name = "Interface builder",
    label = "Interface builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"form_id",false),
      JSONField(JSONFieldTypes.STRING,"name",false),
      JSONField(JSONFieldTypes.STRING,"description",true),
      JSONField(JSONFieldTypes.STRING,"layout",true, widget = Some(WidgetsNames.textarea),label = Some("")),
      JSONField(JSONFieldTypes.STRING,"entity",false,lookup = Some(JSONFieldLookup.prefilled(
        tables.map(x => JSONLookup(x,x))
      ))),
      JSONField(JSONFieldTypes.STRING,"tabularFields",false),
      JSONField(JSONFieldTypes.STRING,"query",true),
      JSONField(JSONFieldTypes.STRING,"exportFields",true),
      JSONField(JSONFieldTypes.CHILD,"fields",true,child = Some(Child(2,"fields","form_id","form_id",None))),
      JSONField(JSONFieldTypes.CHILD,"form_i18n",true,child = Some(Child(4,"form_i18n","form_id","form_id",None)))
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,8,Seq(
          SubLayoutBlock(None,Seq(5,1,6),Seq(
            Right(
              SubLayoutBlock(Some("Base Info"),Seq(12),Seq("form_id","name","entity","query","description").map(Left(_)))
            ),
            Left(""),
            Right(
              SubLayoutBlock(Some("Table Info"),Seq(12),Seq("tabularFields","exportFields").map(Left(_)))
            )
          )),
          SubLayoutBlock(Some("Layout"),Seq(12),Seq("layout").map(Left(_))),
        ).map(Right(_))),
        LayoutBlock(Some("I18n"),4,Seq("form_i18n").map(Left(_))),
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

  def field(forms:Seq[Form.Form_row]) = JSONMetadata(
    objId = FIELD,
    name = "Field builder",
    label = "Field builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"field_id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.NUMBER,"form_id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.STRING,"name",false),
      JSONField(JSONFieldTypes.STRING,"widget",true,lookup = Some(JSONFieldLookup.prefilled(
        WidgetsNames.all.map(x => JSONLookup(x,x))
      ))),
      JSONField(JSONFieldTypes.STRING,"type",false,lookup = Some(JSONFieldLookup.prefilled(
        JSONFieldTypes.ALL.sorted.map(x => JSONLookup(x,x))
      ))),
      JSONField(JSONFieldTypes.CHILD,"field_i18n",true,child = Some(Child(3,"field_i18n","field_id","field_id",None))),
      JSONField(JSONFieldTypes.CHILD,"field_file",true,child = Some(Child(5,"field_file","field_id","field_id",None)),condition = Some(ConditionalField("type",Seq(JSONFieldTypes.FILE.asJson)))),
      JSONField(JSONFieldTypes.STRING,"lookupEntity",true),
      JSONField(JSONFieldTypes.STRING,"lookupValueField",true),
      JSONField(JSONFieldTypes.STRING,"lookupQuery",true),
      JSONField(JSONFieldTypes.NUMBER,"child_form_id",true,
        lookup = Some(JSONFieldLookup.prefilled(
          forms.map{ form => JSONLookup(form.form_id.get.toString,form.name) }
        )),
        condition = Some(ConditionalField("type",Seq(JSONFieldTypes.CHILD.asJson)))
      ),
      JSONField(JSONFieldTypes.STRING,"masterFields",true,condition = Some(ConditionalField("type",Seq(JSONFieldTypes.CHILD.asJson)))),
      JSONField(JSONFieldTypes.STRING,"childFields",true,condition = Some(ConditionalField("type",Seq(JSONFieldTypes.CHILD.asJson)))),
      JSONField(JSONFieldTypes.STRING,"childQuery",true,condition = Some(ConditionalField("type",Seq(JSONFieldTypes.CHILD.asJson)))),
      JSONField(JSONFieldTypes.STRING,"default",true),
      JSONField(JSONFieldTypes.NUMBER,"min",true,condition = Some(ConditionalField("type",Seq(JSONFieldTypes.NUMBER.asJson)))),
      JSONField(JSONFieldTypes.NUMBER,"max",true,condition = Some(ConditionalField("type",Seq(JSONFieldTypes.NUMBER.asJson)))),
      JSONField(JSONFieldTypes.STRING,"conditionFieldId",true),
      JSONField(JSONFieldTypes.STRING,"conditionValues",true),
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,4,Seq(
          "field_id",
          "form_id",
          "name",
          "type",
          "widget",
          "lookupEntity",
          "lookupValueField",
          "lookupQuery",
          "child_form_id",
          "masterFields",
          "childFields",
          "childQuery",
          "default",
          "min",
          "max",
          "conditionFieldId",
          "conditionValues",
          "field_file"
        ).map(Left(_))),
        LayoutBlock(None,8,Seq("field_i18n").map(Left(_))),
      )
    ),
    entity = "field",
    lang = "en",
    tabularFields = Seq("field_id","form_id","name","widget"),
    keys = Seq("field_id"),
    query = None,
    exportFields = Seq()
  )

  val fieldI18n = JSONMetadata(
    objId = 3,
    name = "FieldI18n builder",
    label = "FieldI18n builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"field_id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.NUMBER,"id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.STRING,"lang",false),
      JSONField(JSONFieldTypes.STRING,"label",true),
      JSONField(JSONFieldTypes.STRING,"tooltip",true),
      JSONField(JSONFieldTypes.STRING,"hint",true),
      JSONField(JSONFieldTypes.STRING,"placeholder",true),
      JSONField(JSONFieldTypes.STRING,"lookupTextField",true),
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,3,Seq("field_id","id","lang").map(Left(_))),
        LayoutBlock(None,9,Seq("label","placeholder","tooltip","hint","lookupTextField").map(Left(_))),
      )
    ),
    entity = "field_i18n",
    lang = "en",
    tabularFields = Seq("field_id","id","lang","label"),
    keys = Seq("id"),
    query = None,
    exportFields = Seq()
  )

  val formI18n = JSONMetadata(
    objId = 4,
    name = "FormI18n builder",
    label = "FormI18n builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"form_id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.NUMBER,"id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.STRING,"lang",false,widget = Some(WidgetsNames.fullWidth)),
      JSONField(JSONFieldTypes.STRING,"label",true),
      JSONField(JSONFieldTypes.STRING,"tooltip",true),
      JSONField(JSONFieldTypes.STRING,"hint",true),
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,4,Seq("field_id","id","lang").map(Left(_))),
        LayoutBlock(None,8,Seq("label","tooltip","hint").map(Left(_))),
      )
    ),
    entity = "form_i18n",
    lang = "en",
    tabularFields = Seq("form_id","id","lang","label"),
    keys = Seq("id"),
    query = None,
    exportFields = Seq()
  )

  val fieldFile = JSONMetadata(
    objId = 5,
    name = "FieldFile builder",
    label = "FieldFile builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"field_id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.STRING,"file_field",false,widget = Some(WidgetsNames.fullWidth)),
      JSONField(JSONFieldTypes.STRING,"thumbnail_field",false),
      JSONField(JSONFieldTypes.STRING,"name_field",false),
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,12,Seq("field_id","file_field","thumbnail_field","name_field").map(Left(_))),
      )
    ),
    entity = "field_file",
    lang = "en",
    tabularFields = Seq("field_id","file_field"),
    keys = Seq("field_id"),
    query = None,
    exportFields = Seq()
  )



  def registry = for{
    forms <- getForms()
  } yield Seq(main(Table.tables.toSeq.sorted),field(forms),fieldI18n,formI18n,fieldFile)

  def getForms():Future[Seq[Form.Form_row]] = {
    up.boxDb.run{
      Form.table.result
    }
  }

  override def list: Future[Seq[String]] = registry.map(_.map(_.name))

  override def of(name: String, lang: String): Future[JSONMetadata] = registry.map(_.find(_.name == name).get)

  override def of(id: Int, lang: String): Future[JSONMetadata] = registry.map(_.find(_.objId == id).get)

  override def children(form: JSONMetadata): Future[Seq[JSONMetadata]] = getForms().map{ forms =>
    form match {
      case f if f.objId == FORM => Seq(field(forms),fieldI18n,formI18n,fieldFile)
      case f if f.objId == FIELD => Seq(fieldI18n,fieldFile)
      case _ => Seq()
    }
  }

}
