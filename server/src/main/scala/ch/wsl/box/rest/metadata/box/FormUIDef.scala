package ch.wsl.box.rest.metadata.box

import ch.wsl.box.model.boxentities.BoxForm
import ch.wsl.box.model.boxentities.BoxUser.BoxUser_row
import ch.wsl.box.model.shared._
import ch.wsl.box.rest.utils.BoxConfig

object FormUIDef {

  import io.circe._
  import io.circe.syntax._
  import Constants._

  def main(tables:Seq[String], users:Seq[BoxUser_row]) = JSONMetadata(
    objId = FORM,
    name = "form",
    label = "Interface builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"form_id",false,widget = Some(WidgetsNames.inputDisabled)),
      JSONField(JSONFieldTypes.STRING,"name",false,widget = Some(WidgetsNames.textinput)),
      JSONField(JSONFieldTypes.STRING,"description",true,widget = Some(WidgetsNames.twoLines)),
      JSONField(JSONFieldTypes.STRING,"layout",true, widget = Some(WidgetsNames.code),label = Some(""),
        params = Some(Json.obj("language" -> "json".asJson, "height" -> 600.asJson))
      ),
      JSONField(JSONFieldTypes.STRING,"entity",false,
        widget = Some(WidgetsNames.select),
        lookup = Some(JSONFieldLookup.prefilled(
          tables.map(x => JSONLookup(x,x))
        ))
      ),
      JSONField(JSONFieldTypes.STRING,"tabularFields",false,widget = Some(WidgetsNames.textinput)),
      JSONField(JSONFieldTypes.STRING,"query",true,
        widget = Some(WidgetsNames.code),
        params = Some(Json.obj("language" -> "json".asJson, "height" -> 100.asJson, "fullWidth" -> false.asJson))
      ),
      JSONField(JSONFieldTypes.STRING,"guest_user",true,
        widget = Some(WidgetsNames.select),
        lookup = Some(JSONFieldLookup.prefilled(
          users.map(x => JSONLookup(x.username,x.username))
        ))
      ),
      JSONField(JSONFieldTypes.STRING,"edit_key_field",true,
        widget = Some(WidgetsNames.textinput),
        label = Some("Key fields"),
        placeholder = Some("by default primary key is used"),
        tooltip = Some("Manually enter the fields that should be used as primary key. This is useful mainly for updatable views where the primary key of the entity cannot be calculated. Fields are separated with comma")
      ),
      JSONField(JSONFieldTypes.STRING,"exportFields",true,widget = Some(WidgetsNames.textinput)),
      JSONField(JSONFieldTypes.CHILD,"fields",true,
        child = Some(Child(FORM_FIELD,"fields","form_id","form_id",Some(JSONQuery.sortByKeys(Seq("field_id"))))),
        widget = Some(WidgetsNames.tableChild)
      ),
      JSONField(JSONFieldTypes.CHILD,"form_i18n",true,
        child = Some(Child(FORM_I18N,"form_i18n","form_id","form_id",Some(JSONQuery.sortByKeys(Seq("lang"))))),
        widget = Some(WidgetsNames.tableChild)
      )
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,8,Seq(
          SubLayoutBlock(None,Seq(12,12,12),Seq(
            Right(
              SubLayoutBlock(Some("Base Info"),Seq(12),Seq("name","entity","query","description","guest_user","edit_key_field").map(Left(_)))
            ),
            Left(""),
            Right(
              SubLayoutBlock(Some("Table Info"),Seq(12),Seq("tabularFields","exportFields").map(Left(_)))
            )
          ))
        ).map(Right(_))),
        LayoutBlock(Some("I18n"),4,Seq("form_i18n").map(Left(_))),
        LayoutBlock(Some("Fields"),12,Seq("fields").map(Left(_))),
        LayoutBlock(Some("Layout"),12,Seq("layout").map(Left(_))),
      )
    ),
    entity = "form",
    lang = "en",
    tabularFields = Seq("form_id","name","entity","description"),
    rawTabularFields = Seq("form_id","name","entity","description"),
    keys = Seq("form_id"),
    keyStrategy = SurrugateKey,
    query = None,
    exportFields = Seq(),
    view = None,
    action = FormActionsMetadata.default
  )

  def field(forms:Seq[BoxForm.BoxForm_row],tables:Seq[String]) = JSONMetadata(
    objId = FORM_FIELD,
    name = "Field builder",
    label = "Field builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"field_id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.NUMBER,"form_id",false,widget = Some(WidgetsNames.hidden)),
      CommonField.name,
      CommonField.widget,
      CommonField.typ,
      JSONField(JSONFieldTypes.CHILD,"field_i18n",true,child = Some(Child(FORM_FIELD_I18N,"field_i18n","field_id","field_id",Some(JSONQuery.sortByKeys(Seq("field_id"))))), widget = Some(WidgetsNames.tableChild)),
      JSONField(JSONFieldTypes.CHILD,"field_file",true,
        child = Some(Child(FORM_FIELD_FILE,"field_file","field_id","field_id",None)),
        condition = Some(ConditionalField("type",Seq(JSONFieldTypes.FILE.asJson))),
        params = Some(Map("max" -> 1, "min" -> 1).asJson),
        widget = Some(WidgetsNames.simpleChild)
      ),
      CommonField.lookupEntity(tables),
      CommonField.lookupValueField(tables),
      CommonField.lookupQuery(tables),
      JSONField(JSONFieldTypes.NUMBER,"child_form_id",true,
        widget = Some(WidgetsNames.select),
        lookup = Some(JSONFieldLookup.prefilled(
          forms.map{ form => JSONLookup(form.form_id.get.toString,form.name) }
        )),
        condition = Some(ConditionalField("type",Seq(JSONFieldTypes.CHILD.asJson)))
      ),
      JSONField(JSONFieldTypes.STRING,"masterFields",true,label=Some("Parent field"),
        widget = Some(WidgetsNames.textinput),
        condition = Some(
          ConditionalField("widget",Seq(
            WidgetsNames.simpleChild,
            WidgetsNames.tableChild,
            WidgetsNames.linkedForm,
            WidgetsNames.lookupLabel
          ).map(_.asJson))
        )
      ),
      JSONField(JSONFieldTypes.STRING,"linked_key_fields",true,
        widget = Some(WidgetsNames.textinput),
        condition = Some(ConditionalField("widget",Seq(WidgetsNames.linkedForm.asJson)))
      ),
      JSONField(JSONFieldTypes.STRING,"linked_label_fields",true,
        widget = Some(WidgetsNames.textinput),
        condition = Some(ConditionalField("widget",Seq(WidgetsNames.linkedForm.asJson)))
      ),
      JSONField(JSONFieldTypes.STRING,"childFields",true,
        widget = Some(WidgetsNames.textinput),
        condition = Some(ConditionalField("type",Seq(JSONFieldTypes.CHILD.asJson)))
      ),
      JSONField(JSONFieldTypes.STRING,"childQuery",true,
        condition = Some(ConditionalField("type",Seq(JSONFieldTypes.CHILD.asJson))),
        widget = Some(WidgetsNames.code),
        params = Some(Json.obj("language" -> "json".asJson, "height" -> 200.asJson))
      ),
      CommonField.default,
      JSONField(JSONFieldTypes.NUMBER,"min",true,
        widget = Some(WidgetsNames.inputNumber),
        condition = Some(ConditionalField("type",Seq(JSONFieldTypes.NUMBER.asJson)))
      ),
      JSONField(JSONFieldTypes.NUMBER,"max",true,
        widget = Some(WidgetsNames.inputNumber),
        condition = Some(ConditionalField("type",Seq(JSONFieldTypes.NUMBER.asJson)))
      ),
      CommonField.conditionFieldId,
      CommonField.conditionValues,
      JSONField(JSONFieldTypes.JSON,"params",true,widget = Some(WidgetsNames.code)),
      JSONField(JSONFieldTypes.BOOLEAN,"read_only",false,default = Some("false"),widget = Some(WidgetsNames.checkbox)),
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,6,Seq(
          "name",
          "type",
          "widget",
          "read_only",
          "lookupEntity",
          "lookupValueField",
          "lookupQuery",
          "child_form_id",
          "masterFields",
          "linked_key_fields",
          "linked_label_fields",
          "childFields",
          "childQuery",
          "default",
          "min",
          "max",
          "conditionFieldId",
          "conditionValues",
          "params",
          "field_file"
        ).map(Left(_))),
        LayoutBlock(None,6,Seq("field_i18n").map(Left(_))),
      )
    ),
    entity = "field",
    lang = "en",
    tabularFields = Seq("field_id","name","widget"),
    rawTabularFields = Seq("name","widget","read_only","lookupEntity","child_form_id"),
    keys = Seq("field_id"),
    keyStrategy = SurrugateKey,
    query = None,
    exportFields = Seq(),
    view = None,
    action = FormActionsMetadata.default
  )

  val fieldI18n = JSONMetadata(
    objId = FORM_FIELD_I18N,
    name = "FieldI18n builder",
    label = "FieldI18n builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"field_id",false, widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.NUMBER,"id",false, widget = Some(WidgetsNames.hidden)),
      CommonField.lang,
      CommonField.label,
      CommonField.tooltip,
      CommonField.hint,
      CommonField.placeholder,
      CommonField.lookupTextField,
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,12,Seq("lang","label","placeholder","tooltip","hint","lookupTextField").map(Left(_))),
      )
    ),
    entity = "field_i18n",
    lang = "en",
    tabularFields = Seq("field_id","id","lang","label"),
    rawTabularFields = Seq("lang","label","lookupTextField"),
    keys = Seq("id"),
    keyStrategy = SurrugateKey,
    query = None,
    exportFields = Seq(),
    view = None,
    action = FormActionsMetadata.default
  )

  def formI18n(views:Seq[String]) = JSONMetadata(
    objId = FORM_I18N,
    name = "FormI18n builder",
    label = "FormI18n builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"form_id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.NUMBER,"id",false,widget = Some(WidgetsNames.hidden)),
      CommonField.lang,
      CommonField.label,
      JSONField(JSONFieldTypes.STRING,"view_table",true,
        widget = Some(WidgetsNames.select),
        lookup = Some(JSONFieldLookup.prefilled(
          views.map(x => JSONLookup(x,x))
        ))
      ),
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,12,Seq("lang","label","view_table").map(Left(_))),
      )
    ),
    entity = "form_i18n",
    lang = "en",
    tabularFields = Seq("form_id","id","lang","label"),
    rawTabularFields = Seq("lang","label","view_table"),
    keys = Seq("id"),
    keyStrategy = SurrugateKey,
    query = None,
    exportFields = Seq(),
    view = None,
    action = FormActionsMetadata.default
  )

  val fieldFile = JSONMetadata(
    objId = FORM_FIELD_FILE,
    name = "FieldFile builder",
    label = "FieldFile builder",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"field_id",false,widget = Some(WidgetsNames.hidden)),
      JSONField(JSONFieldTypes.STRING,"file_field",false,widget = Some(WidgetsNames.textinput)),
      JSONField(JSONFieldTypes.STRING,"thumbnail_field",false,widget = Some(WidgetsNames.textinput)),
      JSONField(JSONFieldTypes.STRING,"name_field",false,widget = Some(WidgetsNames.textinput)),
    ),
    layout = Layout(
      blocks = Seq(
        LayoutBlock(None,12,Seq("file_field","thumbnail_field","name_field").map(Left(_))),
      )
    ),
    entity = "field_file",
    lang = "en",
    tabularFields = Seq("field_id","file_field"),
    rawTabularFields = Seq("field_id","file_field"),
    keys = Seq("field_id"),
    keyStrategy = NaturalKey,
    query = None,
    exportFields = Seq(),
    view = None,
    action = FormActionsMetadata.default
  )
}
