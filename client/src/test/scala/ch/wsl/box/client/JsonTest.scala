package ch.wsl.box.client

//import ch.wsl.box.model.shared.{JSONSchema, JSONSchemaL2, JSONSchemaL3}
//import utest._
//
//object JsonTest extends TestSuite{
//
//  val jsonSchema = """{"type":"object","title":"val_a","properties":{"id":{"type":"number","title":"id","readonly":false,"order":1},"name":{"type":"string","title":"name","readonly":false,"order":2}},"required":["id"],"readonly":false}"""
//  //val jsonSchema = """{"type":"object","readonly":false}"""
//
//
//
//  val tests = this{
//    'test1{
//      import io.circe.parser.decode
//      import io.circe.generic.auto._
//
//      val result = decode[JSONSchema](jsonSchema)
//    }
////    'test2{
////      GenCodec.read[Js.Value](new SimpleValueInput(json2))
////    }
//  }
//}
