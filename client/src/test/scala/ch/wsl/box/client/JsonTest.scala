package ch.wsl.box.client

import ch.wsl.box.model.shared.{JSONSchema, JSONSchemaL2, JSONSchemaL3}
import upickle.Js
import utest._

object JsonTest extends TestSuite{

  //val jsonSchema = """{"type":"object","title":"val_a","properties":{"id":{"type":"number","title":"id","readonly":false,"order":1},"name":{"type":"string","title":"name","readonly":false,"order":2}},"required":["id"],"readonly":false}"""
  //val jsonSchema = """{"type":"object","title":"val_a","properties":{"id":{"type":"number","title":"id","readonly":false,"order":1},"name":{"type":"string","title":"name","readonly":false,"order":2}},"required":["id"],"readonly":false}"""
  //val jsonSchema = """{"type":"object","title":"val_a","properties":{"id":{"type":"number","title":"id","readonly":false,"order":1},"name":{"type":"string","title":"name","readonly":false,"order":2}},"required":["id"],"readonly":false}"""
  //val jsonSchema = """{"type":"object","title":"val_a","properties":{"id":{"type":"number","title":"id","readonly":false,"order":1},"name":{"type":"string","title":"name","readonly":false,"order":2}},"required":["id"],"readonly":false}"""
  val jsonSchema = """{"type":"object","readonly":false}"""



  val tests = this{
    'test1{
      import upickle.default._
      //import ch.wsl.box.client.formatters.Formatters._

      implicit val jsonschemal3F: ReadWriter[JSONSchemaL3] = macroRW[JSONSchemaL3]
      implicit val jsonschemal2F: ReadWriter[JSONSchemaL2] = macroRW[JSONSchemaL2]

      val result = read[JSONSchema](jsonSchema)
      println(result)
    }
//    'test2{
//      GenCodec.read[Js.Value](new SimpleValueInput(json2))
//    }
  }
}
