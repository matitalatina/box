package ch.wsl.box.rest.metadata.box

import ch.wsl.box.model.shared.{Child, FormActionsMetadata, JSONField, JSONFieldTypes, JSONMetadata, Layout, LayoutBlock, SubLayoutBlock, SurrugateKey, WidgetsNames}
import ch.wsl.box.rest.metadata.FormMetadataFactory
import ch.wsl.box.rest.metadata.box.Constants.{LABEL, LABEL_CONTAINER, PAGE}

object LabelUIDef {

  val labelContainer = JSONMetadata(
    objId = LABEL_CONTAINER,
    name = "labels",
    label = "Labels",
    fields = Seq(
      JSONField(JSONFieldTypes.CHILD,"labels",false,
        widget = Some(WidgetsNames.editableTable),
        child = Some(Child(
          objId = LABEL,
          key = "labels",
          mapping = Seq(),
          childQuery = None
        ))
      )
    ),
    Layout(
      blocks = Seq(
        LayoutBlock(None,12,Seq("labels").map(Left(_))),
      )
    ),
    entity = FormMetadataFactory.STATIC_PAGE,
    lang = "en",
    tabularFields = Seq(),
    rawTabularFields = Seq(),
    keys = Seq(),
    keyStrategy = SurrugateKey,
    query = None,
    exportFields = Seq(),
    view = None,
    action = FormActionsMetadata.default
  )

  val label = JSONMetadata(
    objId = LABEL,
    name = "label",
    label = "Labels",
    fields = Seq(
      JSONField(JSONFieldTypes.NUMBER,"id",false,widget = Some(WidgetsNames.inputDisabled)),
      CommonField.lang,
      JSONField(JSONFieldTypes.STRING,"key",false,widget = Some(WidgetsNames.input)),
      JSONField(JSONFieldTypes.STRING,"label",true,widget = Some(WidgetsNames.input))
    ),
    Layout(
      blocks = Seq(
        LayoutBlock(None,12,Seq("id","lang","key","label").map(Left(_))),
      )
    ),
    entity = "labels",
    lang = "en",
    tabularFields = Seq("id","lang","key","label"),
    rawTabularFields = Seq("id","lang","key","label"),
    keys = Seq("id"),
    keyStrategy = SurrugateKey,
    query = None,
    exportFields = Seq(),
    view = None,
    action = FormActionsMetadata.default
  )
}
