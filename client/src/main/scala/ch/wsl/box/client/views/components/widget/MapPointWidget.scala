package ch.wsl.box.client.views.components.widget

import ch.wavein.leaflet._
import ch.wsl.box.client.utils.ClientConf
import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import io.udash.properties.single.Property
import scalatags.JsDom
import scalatags.JsDom.all._
import scribe.Logging
import io.udash._

import scala.util.Try


case class MapPointWidget(id: Property[String], field: JSONField, prop: Property[Json]) extends Widget with Logging {


  import io.circe.syntax._
  import io.circe._
  import io.circe.parser._

  val accessToken = ClientConf.mapBoxAccessToken

  val mapDiv = div(height := 300).render

  var editable = false
  var loaded = false


  def jsonToLatLng(js:Json):LatLng = Try{
    val ll = parse(js.as[String].right.get).right.get.as[Seq[Double]].right.get
    LatLng(ll(0),ll(1))
  }.getOrElse(LatLng(0,0))

  def latLngToJson(latLng:LatLng):Json = Seq(latLng.lat,latLng.lng).asJson.toString().asJson

  override def afterRender(): Unit =  {
    if(!loaded) {
      loaded = true
      logger.info("Map point after render")


      val map = Leaflet.map(mapDiv).setView(jsonToLatLng(prop.get), 13)


      Leaflet.tileLayer(
        s"https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=$accessToken",
        new TileLayerOptions {
          override val id = "mapbox.streets"
        }
      ).addTo(map)

      val marker: Marker[_] = Leaflet.marker(jsonToLatLng(prop.get), new MarkerOptions {
        override val draggable = true
      })

      if (editable) {
        marker.on("dragend", (event: LeafletEvent) => {
          logger.info(marker.getLatLng().toString())
          prop.set(latLngToJson(marker.getLatLng()))
        })
      }

      map.addLayer(marker)

      prop.listen{ js =>
        val point = jsonToLatLng(js)
        marker.setLatLng(point)
        map.setView(point,map.getZoom())
      }

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

object MapPointWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField) = MapPointWidget(id,field,prop)
}