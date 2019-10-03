package ch.wsl.box.rest.html.mustache

import java.io.{Reader, StringReader, StringWriter}

import ch.wsl.box.rest.html.Html
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.mustachejava.{DefaultMustacheFactory, MustacheResolver}
import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class Mustache extends Html {
  override def render(html: String, json: Json)(implicit ex:ExecutionContext): Future[String] = render(html,jsValue2Object(json))

  def render(template: String, scope: Object)(implicit ex:ExecutionContext): Future[String] = {
    renderWithComponents(template, Map(), scope)
  }

  def renderWithComponents(template: String, components: Map[String, String], scope: Object)(implicit ex:ExecutionContext): Future[String] = Future{
    val reader = new StringReader(template)
    val mf = new DefaultMustacheFactory(new InMemoryMustacheResolver(components))
    mf.setObjectHandler(new ScalaObjectHandler())

    val mustache = mf.compile(reader, "mustache-tmpl-" + Random.nextInt() + ".mustache")

    val writer = new StringWriter()

    mustache.execute(writer, scope).flush()
    val result = writer.toString
    writer.close()

    result
  }

  def toMustacheParsableObj(data: String): java.util.Map[String, Object] = {
    val mapper = new ObjectMapper()
    mapper.readValue(data, classOf[java.util.Map[String, Object]])
  }

  def jsValue2Object(json:Json):Object = {
    toMustacheParsableObj(json.toString())
  }

}

class InMemoryMustacheResolver(components:Map[String,String]) extends MustacheResolver {
  override def getReader(resourceName: String): Reader = {
    new StringReader(components(resourceName.replaceAll(".mustache","")))
  }
}
