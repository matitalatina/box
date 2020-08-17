package ch.wsl.box.client.views.components.widget

import ch.wsl.box.model.shared.JSONField
import io.circe.Json
import io.udash._
import org.scalablytyped.runtime.StringDictionary
import scalatags.JsDom
import scalatags.JsDom.all._
import scribe.Logging
import typings.ol._
import io.circe.scalajs._
import typings.geojson.mod.Geometry
import typings.ol.mod.Feature

import scala.scalajs.js



case class OlMapWidget(id: Property[String], field: JSONField, prop: Property[Json]) extends Widget with Logging {

  val mapDiv = div(height := 400).render


  override def afterRender(): Unit = {

    println("After render")

    //typings error need to map it manually
    typings.proj4.mod.^.asInstanceOf[js.Dynamic].default.defs(
      "EPSG:21781",
      "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=600000 +y_0=200000 +ellps=bessel +towgs84=674.4,15.1,405.3,0,0,0,0 +units=m +no_defs"
    )

    typings.proj4.mod.^.asInstanceOf[js.Dynamic].default.defs(
      "EPSG:2056",
      "+proj=somerc +lat_0=46.95240555555556 +lon_0=7.439583333333333 +k_0=1 +x_0=2600000 +y_0=1200000 +ellps=bessel +towgs84=674.374,15.056,405.346,0,0,0,0 +units=m +no_defs"
    )


    proj4Mod.register(typings.proj4.mod.^.asInstanceOf[js.Dynamic].default)


    val projectionLV03 = new projectionMod.default(projectionMod.Options("EPSG:21781")
        .setUnits("m")
        .setExtent(js.Tuple4(485071.54,75346.36,828515.78,299941.84))
    )

    val projectionLV95 = new projectionMod.default(projectionMod.Options("EPSG:2056")
      .setUnits("m")
      .setExtent(js.Tuple4(2485071.58, 1075346.31, 2828515.82, 1299941.79))
    )

    val raster = new layerMod.Tile(baseTileMod.Options().setSource(new sourceMod.OSM()))
    val swisstopo1000 = new imageMod.default(baseImageMod.Options()
        .setExtent(projectionLV03.getExtent())
        .setSource(new imageWMSMod.default(imageWMSMod.Options(
          url = "https://wms.geo.admin.ch/",
          params = StringDictionary(
            "LAYERS" -> "ch.swisstopo.pixelkarte-farbe-pk1000.noscale",
            "FORMAT" -> "image/jpeg"
          )
        )))
    )

    val vectorSource = new sourceMod.Vector[geometryMod.default](sourceVectorMod.Options())


    val vector = new layerMod.Vector(baseVectorMod.Options()
      .setSource(vectorSource)
//      .setStyle(
//        new styleMod.Style(styleStyleMod.Options()
//          .setFill(new styleMod.Fill(fillMod.Options().setColor("rgba(255, 255, 255, 0.2)")))
//          .setStroke(new styleMod.Stroke(strokeMod.Options().setColor("#ffcc33").setWidth(2)))
//          .setImage(
//            new styleMod.Circle(styleCircleMod.Options(7)
//              .setFill(
//                new styleMod.Fill(fillMod.Options().setColor("rgba(255, 255, 255, 0.2)"))
//              )
//            )
//          )
//        )
//      )
    )

    val controls = controlMod.defaults().extend(js.Array(new controlMod.ScaleLine()))


    val view = new viewMod.default(viewMod.ViewOptions()
      .setZoom(3)
      .setProjection(projectionLV03)
      .setCenter(js.Array(676813.12, 245983.95))
    )



    val map = new mod.Map(pluggableMapMod.MapOptions()
      .setLayers(js.Array[baseMod.default](raster,vector))
      .setTarget(mapDiv)
      .setControls(controls.getArray())
      .setView(view)
    )


    prop.listen({ geoData =>
      vectorSource.getFeatures().foreach(f => vectorSource.removeFeature(f))
      val point = new geoJSONMod.default().readFeature(convertJsonToJs(geoData).asInstanceOf[js.Object])
      vectorSource.addFeature(point.asInstanceOf[olFeatureMod.default[geometryMod.default]])
      view.setCenter(point.asInstanceOf[js.Dynamic].getGeometry().getCoordinates().asInstanceOf[js.Array[Double]])
    },initUpdate = true)



//    val modify = new modifyMod.default(modifyMod.Options().setSource(vectorSource))
//    map.addInteraction(modify)
//
//    val draw = new drawMod.default(drawMod.Options(geometryTypeMod.default.POINT)
//      .setSource(vectorSource)
//    )
//
//    map.addInteraction(draw)
//
//    val snap = new snapMod.default(snapMod.Options().setSource(vectorSource))
//
//    map.addInteraction(snap)
  }

  override protected def show(): JsDom.all.Modifier = {
    div(
      label(field.title),
      mapDiv
    )
  }

  override protected def edit(): JsDom.all.Modifier = {
    div(
      label(field.title),
      mapDiv
    )
  }
}

object OlMapWidget extends ComponentWidgetFactory {
  override def create(id: _root_.io.udash.Property[String], prop: _root_.io.udash.Property[Json], field: JSONField) = OlMapWidget(id,field,prop)
}
