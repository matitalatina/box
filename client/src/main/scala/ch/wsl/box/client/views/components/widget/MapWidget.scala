package ch.wsl.box.client.views.components.widget

import ch.wavein.leaflet.{Layer, _}
import ch.wavein.leaflet.geojson.GeoJson.{Geometry, GeometryObject}
import ch.wavein.leaflet.geojson.{Feature, GeoJsonObject, LineString}
import ch.wsl.box.client.utils.{BrowserConsole, ClientConf}
import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import io.udash.properties.single.Property
import scalatags.JsDom
import scalatags.JsDom.all._
import scribe.Logging
import io.udash._

import scala.scalajs.js
import scala.util.Try
import io.circe.scalajs._
import io.udash.Registration
import io.udash.bindings.inputs.{Checkbox, RadioButtons}


import scala.scalajs.js.UndefOr

case class MapWidget(id: Property[String], field: JSONField, prop: Property[Json]) extends Widget with Logging {


  import io.circe._
  import io.circe.generic.auto._
  import io.circe.parser._
  import io.circe.syntax._


  import scalacss.ScalatagsCss._


  val accessToken = ClientConf.mapBoxAccessToken

  val mapDiv = div(height := 400).render
  val addPolygon = button(ClientConf.style.boxButton,"Add polygon").render
  //val addLine = button(ClientConf.style.boxButton,"Add line").render

  var editable = false
  var loaded = false

  val forrestEnable:Property[Boolean] = Property(false)
  val baseMap:Property[String] = Property("osm")





  override def afterRender(): Unit =  {
    if(!loaded) {
      loaded = true
      logger.info("Map point after render")


      val options = new MapOptionsEditable {
        override val editable = true
      }

      options.crs = Leaflet.CRS.EPSG2056
//      options.maxBounds = Leaflet.latLngBounds(
//        southWest = Leaflet.latLng(2485000, 1075000),
//        northEast = Leaflet.latLng(2835000, 1295000)
//      )
//      options.center = LatLng(2716829.428, 1095885.438)
//      options.zoom = 8.0

      val map = Leaflet.map(mapDiv,options).asInstanceOf[MapEditable]

      map.setView(LatLng(1154240.0, 2689920.0),16)

      val osm = Leaflet.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", new TileLayerOptions {

      })

      val popup = Leaflet.popup()
      map.on("click",{case e:LeafletEvent =>
        val latlng = e.asInstanceOf[js.Dynamic].latlng.asInstanceOf[LatLng]
        popup
          .setLatLng(latlng)
          .setContent("You clicked the map at " + latlng.toString())
          .openOn(map)
      })

      val wmsOptions = new WMSOptions {}
      wmsOptions.crs = Leaflet.CRS.EPSG2056
      wmsOptions.layers = "ch.swisstopo.pixelkarte-farbe-pk1000.noscale"
      val swisstopo1000:WMS = Leaflet.tileLayer.wms("https://wms.geo.admin.ch/?",wmsOptions)


      val swisstopo25:WMS = Leaflet.asInstanceOf[js.Dynamic].tileLayer.wms("https://wms.geo.admin.ch/?", js.Dictionary(
        "layers" -> "ch.swisstopo.pixelkarte-farbe-pk25.noscale",
        "crs" -> Leaflet.CRS.EPSG3857,
      )).asInstanceOf[WMS]

      val forrest:WMS = Leaflet.asInstanceOf[js.Dynamic].tileLayer.wms("https://wms.geo.admin.ch/?", js.Dictionary(
        "layers" -> "ch.bafu.waldreservate",
        "crs" -> Leaflet.asInstanceOf[js.Dynamic].CRS.EPSG3857,
        "transparent" -> true,
        "format" -> "image/png",
        "opacity" -> 0.5
      )).asInstanceOf[WMS]


      swisstopo1000.addTo(map)

//      baseMap.listen({ x =>
//        map.removeLayer(swisstopo25)
//        map.removeLayer(swisstopo1000)
//        map.removeLayer(osm)
//        x match {
//          case "osm" => osm.addTo(map)
//          case "swisstopo25" => swisstopo25.addTo(map)
//          case "swisstopo1000" => swisstopo1000.addTo(map)
//        }
//      })

      forrestEnable.listen({
        case true => forrest.addTo(map)
        case false => map.removeLayer(forrest)
      })



      var layers:List[Layer] = List()
      var changeListener:Registration = null;


      val addLayer: js.Function2[Feature[Geometry, js.Any], Layer, Unit] = { (f,l) =>
        layers = l :: layers
        l.addTo(map)
        l.asInstanceOf[EditableMixin].enableEdit()
      }

      def setListener(immediate: Boolean) = {
        changeListener = prop.listen({ geoData =>
          layers.map(x => map.removeLayer(x))
          Leaflet.geoJSON[js.Any](convertJsonToJs(geoData).asInstanceOf[GeoJsonObject], new GeoJSONOptions[js.Any] {
            override val onEachFeature = addLayer
          })

        }, immediate)
      }

      setListener(true)

      addPolygon.onclick = { e =>
        map.editTools.startPolygon(map.getCenter())
      }



      def eventCallback:js.Function1[LeafletEvent,Unit] = {e =>
        layers.map{ l =>
          //BrowserConsole.log(e.asInstanceOf[js.Dynamic].layer.toGeoJSON().geometry)
          convertJsToJson(e.asInstanceOf[js.Dynamic].layer.toGeoJSON().geometry).fold({ e =>
            e.printStackTrace()
          },{j =>
            changeListener.cancel()
            prop.set(j)
            setListener(false)
          })

        }
      }


      map.on("editable:editing",eventCallback,"")




    }


  }

  override protected def show(): JsDom.all.Modifier = {
    div(
      label(field.title),
      mapDiv
    )
  }

  import org.scalajs.dom.html.Input

  override protected def edit(): JsDom.all.Modifier = {
    editable = true
    div(
      div(label(field.title)),
      div(addPolygon),
      mapDiv
    )
  }

}

object MapWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField) = MapWidget(id,field,prop)
}
