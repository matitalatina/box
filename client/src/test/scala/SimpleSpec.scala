import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.{FlatSpec, Matchers}
import postgresweb.models.JSONField


/**
  * Created by andreaminetti on 23/02/16.
  */
class SimpleSpec extends FlatSpec with Matchers {

  val jsonForm = "[{\"type\":\"number\",\"key\":\"categoryId\"},{\"type\":\"text\",\"key\":\"en\"},{\"type\":\"text\",\"key\":\"it\"},{\"type\":\"text\",\"key\":\"ge\"},{\"type\":\"text\",\"key\":\"fr\"}]"


  "A form" should "be read" in {

    val form =  decode[Vector[JSONField]](jsonForm)

    println(form)

    form.toOption.get.head.`type` shouldBe "number"

  }

  case class Test(`type`:String, key:Json)

  "Any Parser" should "work" in {
    val t = decode[Vector[Test]](jsonForm)
    println(t)
  }


}
