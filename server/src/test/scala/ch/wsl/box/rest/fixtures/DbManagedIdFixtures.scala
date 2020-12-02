package ch.wsl.box.rest.fixtures

object DbManagedIdFixtures{
  val layers = Map(
    1 -> s"""
            |{
            |  "name": "parent",
            |  "childs" : [
            |  ]
            |}
    """.stripMargin.trim,
    2 -> s"""
            |{
            |  "name": "parent",
            |  "childs": [
            |     {
            |       "name": "child"
            |     }
            |  ]
            |}
    """.stripMargin.trim,
    3 -> s"""
            |{
            |  "name": "parent",
            |  "childs": [
            |     {
            |       "name": "child",
            |       "subchilds": [
            |         {
            |           "name": "subchild"
            |         }
            |       ]
            |     }
            |  ]
            |}
    """.stripMargin.trim
  )
}