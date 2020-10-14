package ch.wsl.box.client.mocks

import ch.wsl.box.model.shared.{Child, FormActionsMetadata, JSONField, JSONFieldTypes, JSONMetadata, Layout, LayoutBlock, WidgetsNames}

object Values {
  val headerLangEn = "test header en"
  val headerLangIt = "test header it"

  val uiConf = Map(
    "title" -> "Test Title"
  )

  val testFormName = "test_form"
  val testFormTitle = "test form"

  val formEntities = Seq(testFormName)

  val metadata = JSONMetadata(
    1,
    testFormName,
    testFormTitle,
    fields = Seq(
      JSONField(
        JSONFieldTypes.NUMBER,
        "id",
        false
      ),
      JSONField(
        JSONFieldTypes.CHILD,
        name = "child",
        widget = Some(WidgetsNames.tableChild),
        nullable = false,
        child = Some(Child(
          2,
          "id",
          "id",
          "parent_id",
          None
        ))
      )
    ),
    layout = Layout(Seq(LayoutBlock(None,12,Seq(Left("child"))))),
    entity = "test",
    lang = "it",
    tabularFields = Seq("id"),
    rawTabularFields = Seq("id"),
    keys = Seq("id"),
    query = None,
    exportFields = Seq("id"),
    view = None,
    action = FormActionsMetadata.default
  )

}
