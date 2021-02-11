package ch.wsl.box.rest.metadata.box

import ch.wsl.box.model.shared.{Child, ConditionalField, JSONField, JSONFieldLookup, JSONFieldTypes, JSONLookup, JSONQuery, WidgetsNames}
import ch.wsl.box.rest.metadata.box.Constants.{FORM_FIELD_FILE, FORM_FIELD_I18N}
import ch.wsl.box.rest.utils.BoxConfig
import io.circe.Json
import io.circe.syntax._

object CommonField {


  val name = JSONField(JSONFieldTypes.STRING,"name",false,widget = Some(WidgetsNames.textinput))

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
    condition = Some(ConditionalField("widget",Seq(WidgetsNames.select.asJson,WidgetsNames.popup.asJson)))
  )

  def lookupValueField(tables:Seq[String]) =  JSONField(JSONFieldTypes.STRING,"lookupValueField",true,
    condition = Some(ConditionalField("lookupEntity",tables.map(_.asJson))),
    widget = Some(WidgetsNames.textinput)
  )

  def lookupQuery(tables:Seq[String]) = JSONField(JSONFieldTypes.STRING,"lookupQuery",true,
    widget = Some(WidgetsNames.code),
    condition = Some(ConditionalField("lookupEntity",tables.map(_.asJson))),
    params = Some(Json.obj("language" -> "json".asJson, "height" -> 100.asJson, "fullWidth" -> false.asJson))
  )

  val default = JSONField(JSONFieldTypes.STRING,"default",true,widget = Some(WidgetsNames.textinput))

  val conditionFieldId = JSONField(JSONFieldTypes.STRING,"conditionFieldId",true,widget = Some(WidgetsNames.textinput))
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


  val label = JSONField(JSONFieldTypes.STRING,"label",true, widget = Some(WidgetsNames.textinput))
  val tooltip = JSONField(JSONFieldTypes.STRING,"tooltip",true, widget = Some(WidgetsNames.textinput))
  val hint = JSONField(JSONFieldTypes.STRING,"hint",true, widget = Some(WidgetsNames.textinput))
  val placeholder = JSONField(JSONFieldTypes.STRING,"placeholder",true, widget = Some(WidgetsNames.textinput))
  val lookupTextField = JSONField(JSONFieldTypes.STRING,"lookupTextField",true, widget = Some(WidgetsNames.textinput))

}
