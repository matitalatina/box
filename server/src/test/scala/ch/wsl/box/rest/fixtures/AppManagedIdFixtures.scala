package ch.wsl.box.rest.fixtures

object AppManagedIdFixtures{
  val layers = Map(
    1 -> s"""
            |{
            |  "id": 1,
            |  "name": "parent",
            |  "childs" : [
            |  ]
            |}
    """.stripMargin.trim,
    2 -> s"""
            |{
            |  "id": 1,
            |  "name": "parent",
            |  "childs": [
            |     {
            |       "id": 1,
            |       "name": "child",
            |       "parent_id": 1,
            |       "subchilds": []
            |     }
            |  ]
            |}
    """.stripMargin.trim,
    3 -> s"""
            |{
            |  "id": 1,
            |  "name": "parent",
            |  "childs": [
            |     {
            |       "id": 1,
            |       "name": "child",
            |       "parent_id": 1,
            |       "subchilds": [
            |         {
            |           "id": 1,
            |           "name": "subchild",
            |           "child_id": 1
            |         }
            |       ]
            |     }
            |  ]
            |}
    """.stripMargin.trim
  )
}