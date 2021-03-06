package ch.wsl.box.client.views.components.widget

import ch.wsl.box.client.views.components.widget.child.{EditableTable, LookupFormWidget, SimpleChildFactory, TableChildFactory}
import ch.wsl.box.client.views.components.widget.labels.{LinkedFormWidget, LookupLabelWidget, StaticTextWidget, TitleWidget}
import ch.wsl.box.client.views.components.widget.lookup.{PopupWidgetFactory, SelectWidgetFactory}
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

    LookupLabelWidget,

    InputWidgetFactory.Input,
    InputWidgetFactory.InputDisabled,
    InputWidgetFactory.TwoLines,
    InputWidgetFactory.TextArea,

    CheckboxWidget,

    DateTimeWidget.Time,
    DateTimeWidget.Date,
    DateTimeWidget.DateTime,

    TableChildFactory,
    SimpleChildFactory,
    EditableTable,
    LookupFormWidget,
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
      InputWidgetFactory.Input
    }
  }

  @deprecated
  def forType(typ:String):ComponentWidgetFactory = {
    logger.warn("Selecting widget for type is deprecated, specify widget name instead")
    val widgetName = WidgetsNames.defaults.getOrElse(typ,s"no default widget for $typ")
    forName(widgetName)
  }

}
