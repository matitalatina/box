package ch.wsl.box.client.views.components.widget

import ch.wsl.box.client.views.components.{SimpleChildFactory, TableChildFactory}
import ch.wsl.box.client.views.components.widget.labels.{LinkedFormWidget, StaticTextWidget, TitleWidget}
import ch.wsl.box.model.shared.WidgetsNames
import scribe.Logging

object WidgetRegistry extends Logging {
  val widgets:Seq[ComponentWidgetFactory] = Seq(

    TitleWidget(1),
    TitleWidget(2),
    TitleWidget(3),
    TitleWidget(4),
    TitleWidget(5),
    StaticTextWidget,

    HiddenWidget,

    PopupWidgetFactory,
    SelectWidgetFactory,

    InputWidgetFactory.TextDisabled,
    InputWidgetFactory.Number,
    InputWidgetFactory.NumberArray,
    InputWidgetFactory.TextNoLabel,
    InputWidgetFactory.TwoLines,
    InputWidgetFactory.TextArea,
    InputWidgetFactory.Text,
    CheckboxWidget,
    CheckboxNumberWidget,

    DateTimeWidget.Time,
    DateTimeWidget.Date,
    DateTimeWidget.DateTime,
    DateTimeWidget.TimeFullWidth,
    DateTimeWidget.DateFullWidth,
    DateTimeWidget.DateTimeFullWidth,
    DateTimeWidget.Date,
    DateTimeWidget.DateTime,

    TableChildFactory,
    SimpleChildFactory,
    LinkedFormWidget,

    FileSimpleWidgetFactory,
    FileWidgetFactory,

    OlMapWidget,

    MonacoWidget,
    RichTextEditorWidgetFactory(RichTextEditorWidget.Minimal),
    RichTextEditorWidgetFactory(RichTextEditorWidget.Full),
    RedactorFactory,

  )

  def forName(widgetName:String): ComponentWidgetFactory = widgets.find(_.name == widgetName) match {
    case Some(w) => w
    case None => {
      logger.warn(s"Widget: $widgetName not registred, using default")
      InputWidgetFactory.Text
    }
  }

  @deprecated
  def forType(typ:String):ComponentWidgetFactory = {
    logger.warn("Selecting widget for type is deprecated, specify widget name instead")
    val widgetName = WidgetsNames.defaults.getOrElse(typ,s"no default widget for $typ")
    forName(widgetName)
  }

}
