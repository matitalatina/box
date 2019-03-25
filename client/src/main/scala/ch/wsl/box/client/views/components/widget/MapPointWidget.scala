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


case class MapPointWidget(field: JSONField, prop: Property[Json]) extends Widget with Logging {


  val accessToken = ClientConf.mapBoxAccessToken

  var mapDiv = div(height := 300).render



  private def _render(editable:Boolean) = {

    logger.info("Map point after render")


    val map = Leaflet.map(mapDiv).setView(LatLng(51.505, -0.09), 13)


    Leaflet.tileLayer(
      s"https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=$accessToken",
      new TileLayerOptions {
        override val id = "mapbox.streets"
      }
    ).addTo(map)

    val marker: Marker[_] = Leaflet.marker(LatLng(51.505, -0.09), new MarkerOptions {
      override val draggable = true
    })

    if(editable) {
      marker.on("dragend", (event: LeafletEvent) => {
        println(event.target)
        println(event.asInstanceOf[DragEndEvent].distance)
        println(marker.getLatLng())
      })
    }

    map.addLayer(marker)


  }

  override protected def show(): JsDom.all.Modifier = {
    produce(prop) { _ =>
      _render(false)
      mapDiv
    }
  }

  override protected def edit(): JsDom.all.Modifier = {
    produce(prop) { _ =>
      _render(true)
      mapDiv
    }
  }

}

object MapPointWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField) = MapPointWidget(field,prop)
}