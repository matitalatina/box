package ch.wsl.box.client.mocks

import ch.wsl.box.model.shared.{Child, FormActionsMetadata, JSONField, JSONFieldTypes, JSONID, JSONKeyValue, JSONMetadata, Layout, LayoutBlock, WidgetsNames}

object Values {
  val headerLangEn = "test header en"
  val headerLangIt = "test header it"

  val titleId = "titleTest"
  val titleText = "TEST"

  val uiConf = Map(
    "title" -> "Test Title",
    "index.html" -> s"""<div id="$titleId">$titleText</div>"""
  )

  val conf = Map(
    "langs" -> "en,it",
    "display.index.html" -> "true"
  )

  val testFormName = "test_form"
  val testFormTitle = "test form"

  val formEntities = Seq(testFormName)

  val readOnlyField = "read_only_test"
  val readOnlyValue = "read_only_test_value"

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
        JSONFieldTypes.STRING,
        name = readOnlyField,
        nullable = true,
        readOnly = true
      ),
      JSONField(
        JSONFieldTypes.CHILD,
        name = "child",
        widget = Some(WidgetsNames.tableChild),
        nullable = false,
        child = Some(Child(
          objId = 2,
          key = "child",
          masterFields = "id",
          childFields = "parent_id",
          None
        ))
      )
    ),
    layout = Layout(Seq(LayoutBlock(None,12,Seq(Left("child"),Left(readOnlyField))))),
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

  val childMetadata = JSONMetadata(
    2,
    "child",
    "Child form",
    fields = Seq(
      JSONField(
        JSONFieldTypes.NUMBER,
        "id",
        false
      ),
      JSONField(
        JSONFieldTypes.NUMBER,
        name = "parent_id",
        nullable = false
      ),
      JSONField(
        JSONFieldTypes.STRING,
        name = "text",
        nullable = true
      )
    ),
    layout = Layout(Seq(LayoutBlock(None,12,Seq(Left("id"),Left("parent_id"),Left("text"))))),
    entity = "test_child",
    lang = "it",
    tabularFields = Seq("id"),
    rawTabularFields = Seq("id"),
    keys = Seq("id"),
    query = None,
    exportFields = Seq("id"),
    view = None,
    action = FormActionsMetadata.default
  )

  object ids {
    object main {
      val singleChild: JSONID = JSONID(Vector(JSONKeyValue("id", "1")))
      val doubleChild: JSONID = JSONID(Vector(JSONKeyValue("id", "2")))
    }
    object childs {
      val thirdChild: JSONID = JSONID(Vector(JSONKeyValue("id", "3")))
    }

  }

}
