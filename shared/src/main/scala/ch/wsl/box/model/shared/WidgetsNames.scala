package ch.wsl.box.model.shared

import JSONFieldTypes._
/**
  * Created by andreaminetti on 06/06/16.
  */
object WidgetsNames {
  val inputDisabled = "inputDisabled"
  val inputNumber = "inputNumber"
  val inputArrayNumber = "inputArrayNumber"
  val textinput = "textinput"
  val textarea = "textarea"
  val datepicker = "datepicker"
  val timepicker = "timepicker"
  val datetimePicker = "datetimePicker"
  val datepickerFullWidth = "datepickerFullWidth"
  val timepickerFullWidth = "timepickerFullWidth"
  val datetimePickerFullWidth = "datetimePickerFullWidth"
  val select = "selectWidget"
  val checkbox = "checkbox"
  val checkboxNumber = "checkboxNumber"
  val hidden = "hidden"
  val nolabel = "nolabel"
  val twoLines = "twoLines"
  val popup = "popup"
  val fullWidth = "fullWidth"
  val mapPoint = "mapPoint"
  val map = "map"
  val code = "code"
  val richTextEditor = "richTextEditor"
  val richTextEditorFull = "richTextEditorFull"
  val redactor = "redactor"
  val simpleFile = "simpleFile"
  val simpleChild = "simpleChild"
  val tableChild = "tableChild"
  val h1 = "title_h1"
  val h2 = "title_h2"
  val h3 = "title_h3"
  val h4 = "title_h4"
  val h5 = "title_h5"
  val staticText = "static_text"
  val linkedForm = "linked_form"
  val fileWithPreview = "fileWithPreview"
  val lookupLabel = "lookupLabel"

  val mapping= Map(
    NUMBER -> Seq(
      inputNumber,
      select,
      popup,
      checkboxNumber,
      inputDisabled,
      hidden,
    ),
    STRING -> Seq(
      textinput,
      twoLines,
      textarea,
      richTextEditor,
      richTextEditorFull,
      redactor,
      code,
      select,
      popup,
      hidden,
    ),
    CHILD -> Seq(
      simpleChild,
      tableChild,
      linkedForm
    ),
    FILE -> Seq(
      fileWithPreview,
      simpleFile
    ),
    DATE -> Seq(
      datepicker,
      textinput,
      hidden
    ),
    DATETIME -> Seq(
      datetimePicker,
      textinput,
      hidden
    ),
    TIME -> Seq(
      timepicker,
      textinput,
      hidden
    ),
    INTERVAL -> Seq(
      textinput,
      hidden
    ),
    BOOLEAN -> Seq(
      checkbox,
      hidden
    ),
    ARRAY_NUMBER -> Seq(
      inputArrayNumber,
      hidden
    ),
    ARRAY_STRING -> Seq(
      textinput,
      hidden
    ),
    GEOMETRY -> Seq(
      map,
      hidden
    ),
    JSON -> Seq(
      code,
      hidden
    ),
    STATIC -> Seq(
      staticText,
      h1,
      h2,
      h3,
      h4,
      h5,
      lookupLabel
    )
  )

  val defaults = mapping.map{case (k,v) => k -> v.head}  //using defaults is deprecated with starting form interface builder in box 1.3.0

}
