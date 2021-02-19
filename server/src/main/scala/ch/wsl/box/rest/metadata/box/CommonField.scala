package ch.wsl.box.rest.metadata.box

import ch.wsl.box.model.shared.{Child, ConditionalField, JSONField, JSONFieldLookup, JSONFieldTypes, JSONLookup, JSONQuery, JSONQueryFilter, WidgetsNames}
import ch.wsl.box.rest.metadata.box.Constants.{FORM_FIELD_CHILDS, FORM_FIELD_FILE, FORM_FIELD_I18N, FORM_FIELD_STATIC, FORM_I18N}
import ch.wsl.box.rest.utils.BoxConfig
import io.circe.Json
import io.circe.syntax._

object CommonField {


  val name = JSONField(JSONFieldTypes.STRING,"name",false,widget = Some(WidgetsNames.input))

  val widget = JSONField(JSONFieldTypes.STRING,"widget",false,
    widget = Some(WidgetsNames.select),
    lookup = Some(JSONFieldLookup.withExtractor(
      "type",
      WidgetsNames.mapping.map{ case (k,v) => k.asJson -> v.map(x => JSONLookup(x,x))}
    )
  ))

  def typ(child:Boolean = true, static:Boolean = true) = JSONField(JSONFieldTypes.STRING,"type",false,
    widget = Some(WidgetsNames.select),
    lookup = Some(JSONFieldLookup.prefilled(
      JSONFieldTypes.ALL
        .filter(x => child || x != JSONFieldTypes.CHILD)
        .filter(x => static || x != JSONFieldTypes.STATIC)
        .sorted.map(x => JSONLookup(x,x))
    )
  ))

  def lookupEntity(tables:Seq[String]) =  JSONField(JSONFieldTypes.STRING,"lookupEntity",true,
    widget = Some(WidgetsNames.select),
    lookup = Some(JSONFieldLookup.prefilled(
      tables.map(x => JSONLookup(x,x))
    )),
    condition = Some(ConditionalField("widget",Seq(WidgetsNames.select.asJson,WidgetsNames.popup.asJson,WidgetsNames.lookupLabel.asJson)))
  )

  def lookupValueField(tables:Seq[String]) =  JSONField(JSONFieldTypes.STRING,"lookupValueField",true,
    condition = Some(ConditionalField("lookupEntity",tables.map(_.asJson))),
    widget = Some(WidgetsNames.input)
  )

  def lookupQuery(tables:Seq[String]) = JSONField(JSONFieldTypes.STRING,"lookupQuery",true,
    widget = Some(WidgetsNames.code),
    condition = Some(ConditionalField("lookupEntity",tables.map(_.asJson))),
    params = Some(Json.obj("language" -> "json".asJson, "height" -> 100.asJson, "fullWidth" -> false.asJson))
  )

  val default = JSONField(JSONFieldTypes.STRING,"default",true,widget = Some(WidgetsNames.input))

  val conditionFieldId = JSONField(JSONFieldTypes.STRING,"conditionFieldId",true,widget = Some(WidgetsNames.input))
  val conditionValues = JSONField(JSONFieldTypes.STRING,"conditionValues",true,
    widget = Some(WidgetsNames.code),
    placeholder = Some("[1,2,3]"),
    tooltip = Some("Enter a JSON array with the possibles values"),
    params = Some(Json.obj("language" -> "json".asJson, "height" -> 50.asJson, "fullWidth" -> false.asJson))
  )

  val lang = JSONField(JSONFieldTypes.STRING,"lang",false,

    widget = Some(WidgetsNames.select),
    lookup = Some(JSONFieldLookup.prefilled(
      BoxConfig.langs.map(x => JSONLookup(x,x))
    ))
  )


  val label = JSONField(JSONFieldTypes.STRING,"label",true, widget = Some(WidgetsNames.input))
  val tooltip = JSONField(JSONFieldTypes.STRING,"tooltip",true, widget = Some(WidgetsNames.input))
  val hint = JSONField(JSONFieldTypes.STRING,"hint",true, widget = Some(WidgetsNames.input))
  val placeholder = JSONField(JSONFieldTypes.STRING,"placeholder",true, widget = Some(WidgetsNames.input))
  val lookupTextField = JSONField(JSONFieldTypes.STRING,"lookupTextField",true, widget = Some(WidgetsNames.input))


  val formName = JSONField(JSONFieldTypes.STRING,"name",false,widget = Some(WidgetsNames.input))
  val formDescription =JSONField(JSONFieldTypes.STRING,"description",true,widget = Some(WidgetsNames.twoLines))
  val formLayout = JSONField(JSONFieldTypes.STRING,"layout",true, widget = Some(WidgetsNames.code),label = Some(""),
    params = Some(Json.obj("language" -> "json".asJson, "height" -> 600.asJson))
  )

  val formFieldChild = JSONField(JSONFieldTypes.CHILD,"fields_child",true,
    child = Some(Child(FORM_FIELD_CHILDS,"fields_child","form_id","form_id",
      Some(JSONQuery.sortByKeys(Seq("field_id")).filterWith(JSONQueryFilter.WHERE.eq("type",JSONFieldTypes.CHILD)))
    )),
    widget = Some(WidgetsNames.tableChild)
  )
  val formFieldStatic = JSONField(JSONFieldTypes.CHILD,"fields_static",true,
    child = Some(Child(FORM_FIELD_STATIC,"fields_static","form_id","form_id",
      Some(JSONQuery.sortByKeys(Seq("field_id")).filterWith(JSONQueryFilter.WHERE.eq("type",JSONFieldTypes.STATIC)))
    )),
    widget = Some(WidgetsNames.tableChild)
  )

  val formi18n = JSONField(JSONFieldTypes.CHILD,"form_i18n",true,
    child = Some(Child(FORM_I18N,"form_i18n","form_id","form_id",Some(JSONQuery.sortByKeys(Seq("lang"))))),
    widget = Some(WidgetsNames.tableChild)
  )



}
