package ch.wsl.box.client.views.components.widget

import ch.wavein.leaflet._
import ch.wavein.leaflet.geojson.GeoJsonObject
import ch.wsl.box.client.utils.ClientConf
import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import io.udash.properties.single.Property
import scalatags.JsDom
import scalatags.JsDom.all._
import scribe.Logging

import scala.scalajs.js
import scala.util.Try
import io.circe.scalajs._

case class MapWidget(id: Property[String], field: JSONField, prop: Property[Json]) extends Widget with Logging {


  import io.circe._
  import io.circe.parser._
  import io.circe.syntax._

  val accessToken = ClientConf.mapBoxAccessToken

  val mapDiv = div(height := 300).render

  var editable = false
  var loaded = false





  override def afterRender(): Unit =  {
    if(!loaded) {
      loaded = true
      logger.info("Map point after render")


      val map = Leaflet.map(mapDiv).setView(LatLng(0,0),13)

      Leaflet.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png").addTo(map);

      Leaflet.geoJSON(convertJsonToJs(prop.get).asInstanceOf[GeoJsonObject]).addTo(map)


    }


  }

  override protected def show(): JsDom.all.Modifier = {
    div(
      label(field.title),
      mapDiv
    )
  }

  override protected def edit(): JsDom.all.Modifier = {
    editable = true
    div(
      label(field.title),
      mapDiv
    )
  }

}

object MapWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField) = MapWidget(id,field,prop)
}
