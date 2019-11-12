package ch.wsl.box.model.shared

/**
  * Created by andreaminetti on 06/06/16.
  */
object WidgetsNames {
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
  val code = "code"

  val all = Seq(
    textinput,
    textarea,
    datepicker,
    datetimePicker,
    datepickerFullWidth,
    timepickerFullWidth,
    datetimePickerFullWidth,
    select,
    checkbox,
    checkboxNumber,
    hidden,
    nolabel,
    twoLines,
    popup,
    fullWidth,
    mapPoint
  ).sorted
}
