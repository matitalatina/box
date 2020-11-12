//package ch.wsl.box.rest
//
//import io.circe.Json
//import org.graalvm.polyglot.Context
//
//class RIntegrationSpec extends BaseSpec {
//
//
//  "R" should "be run in" in {
//
//    val context = Context.newBuilder().allowAllAccess(true).build()
//
//    val result = context.eval("R",
//      s"""
//         |list(
//         | id   = 42,
//         | text = '42',
//         | arr  = c(1,42,3)
//         |)
//         |""".stripMargin)
//
//    result.hasMembers shouldBe true
//
//    val id = result.getMember("id").asInt()
//    id shouldBe 24
//
//  }
//
//
//}
