package ch.wsl.box.rest.metadata.box

import ch.wsl.box.model.boxentities.Form
import ch.wsl.box.model.shared._

object FunctionUIDef {

  import io.circe._
  import io.circe.syntax._
  import Constants._

  val main = JSONMetadata(
    objId = FUNCTION,
    name = "Function builder",
    label = "Function builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"function_id",false),
      JSONField(JSONFieldTypes.STRING,"name",false),
      JSONField(JSONFieldTypes.STRING,"function",false, widget = Some(WidgetsNames.code+".java"),label = Some(""), default = Some(
        """
          |/*
          | * Must return a Future[DataResult] that represent a CSV table
          | * `context` is provided that has:
          | * - data:io.circe.Json that store the data of the parameters
          | * - ws:ch.wsl.box.rest.logic.RuntimeWS that exposes `get` and `post` methods to interact with external WS
          | * - psql:ch.wsl.box.rest.logic.RuntimePSQL that exposes `function` and `table` to call the underlying DB
          | */
          |
          |for{
          |  result <- Future.successful{
          |     DataResultTable(headers = Seq("demo"), rows = Seq(Seq("demo")))
          |  }
          |} yield result
          |
        """.stripMargin)),
      JSONField(JSONFieldTypes.STRING,"presenter",true, widget = Some(WidgetsNames.code+".handlebars"),label = Some("")),
      JSONField(JSONFieldTypes.STRING,"description",true),
      JSONField(JSONFieldTypes.STRING,"mode",false,lookup = Some(JSONFieldLookup.prefilled(
        FunctionKind.Modes.all.map(x => JSONLookup(x,x))
      ))),
      JSONField(JSONFieldTypes.STRING,"layout",true, widget = Some(WidgetsNames.textarea),label = Some("")),
      JSONField(JSONFieldTypes.NUMBER,"order",true),
      JSONField(JSONFieldTypes.CHILD,"function_field",true,child = Some(Child(FUNCTION_FIELD,"function_field","function_id","function_id",None))),
      JSONField(JSONFieldTypes.CHILD,"function_i18n",true,child = Some(Child(FUNCTION_I18N,"function_i18n","function_id","function_id",None)))
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,8,Seq(
          SubLayoutBlock(None,Seq(5,1,6),Seq(
            Right(
              SubLayoutBlock(Some("Base Info"),Seq(12),Seq("function_id","name","order","description","mode").map(Left(_)))
            ),
            Left(""),
            Right(
              SubLayoutBlock(Some("Layout"),Seq(12),Seq("layout").map(Left(_)))
            )
          )),
          SubLayoutBlock(Some("Function"),Seq(12),Seq("function").map(Left(_))),
          SubLayoutBlock(Some("Presenter"),Seq(12),Seq("presenter").map(Left(_))),
        ).map(Right(_))),
        LayoutBlock(Some("I18n"),4,Seq("function_i18n").map(Left(_))),
        LayoutBlock(Some("Fields"),12,Seq("function_field").map(Left(_))),
      )
    ),
    entity = "function",
    lang = "en",
    tabularFields = Seq("function_id","name","description"),
    keys = Seq("function_id"),
    query = None,
    exportFields = Seq()
  )

  val field = JSONMetadata(
    objId = FUNCTION_FIELD,
    name = "Field builder",
    label = "Field builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"field_id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.NUMBER,"function_id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.STRING,"name",false),
      JSONField(JSONFieldTypes.STRING,"widget",true,lookup = Some(JSONFieldLookup.prefilled(
        WidgetsNames.all.map(x => JSONLookup(x,x))
      ))),
      JSONField(JSONFieldTypes.STRING,"type",false,lookup = Some(JSONFieldLookup.prefilled(
        JSONFieldTypes.ALL.sorted.map(x => JSONLookup(x,x))
      ))),
      JSONField(JSONFieldTypes.CHILD,"function_field_i18n",true,child = Some(Child(FUNCTION_FIELD_I18N,"function_field_i18n","field_id","field_id",None))),
      JSONField(JSONFieldTypes.STRING,"lookupEntity",true),
      JSONField(JSONFieldTypes.STRING,"lookupValueField",true),
      JSONField(JSONFieldTypes.STRING,"lookupQuery",true),
      JSONField(JSONFieldTypes.STRING,"default",true),
      JSONField(JSONFieldTypes.STRING,"conditionFieldId",true),
      JSONField(JSONFieldTypes.STRING,"conditionValues",true),
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,4,Seq(
          "field_id",
          "function_id",
          "name",
          "type",
          "widget",
          "lookupEntity",
          "lookupValueField",
          "lookupQuery",
          "default",
          "conditionFieldId",
          "conditionValues"
        ).map(Left(_))),
        LayoutBlock(None,8,Seq("function_field_i18n").map(Left(_))),
      )
    ),
    entity = "function_field",
    lang = "en",
    tabularFields = Seq("field_id","function_id","name","widget"),
    keys = Seq("field_id"),
    query = None,
    exportFields = Seq()
  )

  val fieldI18n = JSONMetadata(
    objId = FUNCTION_FIELD_I18N,
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
    entity = "function_field_i18n",
    lang = "en",
    tabularFields = Seq("field_id","id","lang","label"),
    keys = Seq("id"),
    query = None,
    exportFields = Seq()
  )

  val functionI18n = JSONMetadata(
    objId = FUNCTION_I18N,
    name = "FormI18n builder",
    label = "FormI18n builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"function_id",false,widget = Some(WidgetsNames.hidden)),
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
    entity = "function_i18n",
    lang = "en",
    tabularFields = Seq("function_id","id","lang","label"),
    keys = Seq("id"),
    query = None,
    exportFields = Seq()
  )



}
